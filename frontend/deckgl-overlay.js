import React, {Component} from 'react';
import DeckGL, {HexagonLayer} from 'deck.gl';

import ShipsLayer from './components/ships-layer';

const LIGHT_SETTINGS = {
  lightsPosition: [-8.42627, 43.32463, 3000],
  ambientRatio: 0.05,
  diffuseRatio: 0.6,
  specularRatio: 0.8,
  lightsStrength: [2.0, 0.0, 0.0, 0.0],
  numberOfLights: 1
};

export default class DeckGLOverlay extends Component {

  static get defaultViewport() {
    return {
      longitude:  -10.87085,
      latitude: 46.0049,
      zoom: 6,
      maxZoom: 16,
      pitch: 45,
      bearing: 0
    };
  }

  _initialize(gl) {
    gl.enable(gl.DEPTH_TEST);
    gl.depthFunc(gl.LEQUAL);
  }

  render() {
    const {viewport, coastal_ports, agents, time} = this.props;

    if (!coastal_ports || !agents) {
      return null;
    }

    const layers = [
      new ShipsLayer({
        id: 'ships',
        data: agents,
        getColor: d => d.vendor === 0 ? [253, 128, 93] : [23, 184, 190],
        opacity: 0.4
      }),

      new HexagonLayer({
          id: 'coastal_ports',
          data: coastal_ports,
          getPosition: e => e.COORDINATES,
          extruded: true,
          elevationRange: [0, 10000],
          radius: 500
      })

    ];

    return (
      <DeckGL {...viewport} layers={layers} onWebGLInitialized={this._initialize} />
    );
  }
}
