import {Layer, assembleShaders} from 'deck.gl';

import {Model, Program, Geometry} from 'luma.gl';

import tripsVertex from './trips-layer-vertex.glsl';
import tripsFragment from './trips-layer-fragment.glsl';

const defaultProps = {
  getColor: d => d.color
};

export default class TripsLayer extends Layer {

  initializeState() {
    const {gl} = this.context;

    const model = this.getModel(this.context.gl);

    this.state.attributeManager.add({
      indices: {size: 1, update: this.calculateIndices, isIndexed: true},
      positions: {size: 3, update: this.calculatePositions},
      colors: {size: 3, update: this.calculateColors}
    });

    this.context.gl.getExtension('OES_element_index_uint');
    this.setState({model});
  }

  updateState({props, changeFlags: {dataChanged}}) {
    if (dataChanged) {
      this.countVertices(props.data);
      this.state.attributeManager.invalidateAll();
    }
  }

  getModel(gl) {
    return new Model({
      program: new Program(gl, assembleShaders(gl, {
        vs: tripsVertex,
        fs: tripsFragment
      })),
      geometry: new Geometry({
        id: this.props.id,
        drawMode: 'LINES'
      }),
      vertexCount: 0,
      isIndexed: true,
      onBeforeRender: () => {
        gl.enable(gl.BLEND);
        gl.enable(gl.POLYGON_OFFSET_FILL);
        gl.polygonOffset(2.0, 1.0);
        gl.blendFunc(gl.SRC_ALPHA, gl.ONE);
        gl.blendEquation(gl.FUNC_ADD);
      },
      onAfterRender: () => {
        gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA);
        gl.disable(gl.POLYGON_OFFSET_FILL);
      }
    });
  }

  countVertices(data) {
    if (!data) {
      return;
    }

    let vertexCount = 0;
    const pathLengths = data.reduce((acc, d) => {
      const l = d.positions.length;
      vertexCount += l;
      return [...acc, l];
    }, []);

    this.setState({pathLengths, vertexCount});
  }

  draw({uniforms}) {
    this.state.model.render(Object.assign({}, uniforms));
  }

  calculateIndices(attribute) {
    const {pathLengths, vertexCount} = this.state;

    const indicesCount = (vertexCount - pathLengths.length) * 2;
    const indices = new Uint32Array(indicesCount);

    let offset = 0;
    let index = 0;
    for (let i = 0; i < pathLengths.length; i++) {
      const l = pathLengths[i];
      indices[index++] = offset;
      for (let j = 1; j < l - 1; j++) {
        indices[index++] = j + offset;
        indices[index++] = j + offset;
      }
      indices[index++] = offset + l - 1;
      offset += l;
    }
    attribute.value = indices;
    this.state.model.setVertexCount(indicesCount);
  }

  calculatePositions(attribute) {
    const {data} = this.props;
    const {vertexCount} = this.state;
    const positions = new Float32Array(vertexCount * 3);

    let index = 0;

    for (let i = 0; i < data.length; i++) {
      const agent_positions = data[i].positions;

      for (let j = 0; j < agent_positions.length; j++) {
          positions[index++] = agent_positions[j].latitude;
          positions[index++] = agent_positions[j].longitude;
          positions[index++] = 1/(1+Math.pow(1.5,-15+7*j)); // This dictates the opacity of the vertex
      }
    }

    attribute.value = positions;
  }

  calculateColors(attribute) {
    const {data, getColor} = this.props;
    const {pathLengths, vertexCount} = this.state;
    const colors = new Float32Array(vertexCount * 3);

    let index = 0;
    for (let i = 0; i < data.length; i++) {
      const color = getColor(data[i]);
      const l = pathLengths[i];
      for (let j = 0; j < l; j++) {
        colors[index++] = color[0];
        colors[index++] = color[1];
        colors[index++] = color[2];
      }
    }
    attribute.value = colors;
  }

}

TripsLayer.layerName = 'TripsLayer';
TripsLayer.defaultProps = defaultProps;
