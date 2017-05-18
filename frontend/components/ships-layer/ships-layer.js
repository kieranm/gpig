import {Layer, assembleShaders} from 'deck.gl';

import {Model, Program, Geometry} from 'luma.gl';

import shipsVertexShader from './ships-layer-vertex.glsl';
import shipsFragmentShader from './ships-layer-fragment.glsl';

export default class ShipsLayer extends Layer {

  initializeState() {
    const {gl} = this.context;

    const model = this.getModel(this.context.gl);

    this.state.attributeManager.add({
      indices: {size: 1, update: this.calculateIndices, isIndexed: true},
      positions: {size: 3, update: this.calculatePositions},
      utilizationRates: {size: 1, update: this.calculateUtilization}
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
        vs: shipsVertexShader,
        fs: shipsFragmentShader
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
        gl.lineWidth(3);
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
          positions[index++] = agent_positions[j].longitude;
          positions[index++] = agent_positions[j].latitude;
          positions[index++] = j; // This dictates the opacity of the vertex
      }
    }

    attribute.value = positions;
  }

  calculateUtilization(attribute) {
    const {data} = this.props;
    const {pathLengths, vertexCount} = this.state;
    const utilization = new Float32Array(vertexCount * 1);

    let index = 0;
    for (let i = 0; i < data.length; i++) {
      const l = pathLengths[i];
      for (let j = 0; j < l; j++) {
        utilization[index++] = data[i].utilization;
      }
    }
    attribute.value = utilization;
  }

}

ShipsLayer.layerName = 'ShipsLayer';