import React, {Component} from 'react';
import DeckGL from 'deck.gl';

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
      longitude:  -3.87085,
      latitude: 53.0049,
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
    const {viewport, agents} = this.props;

    if (!agents) {
      return null;
    }

    const layers = [
      new ShipsLayer({
        id: 'ships',
        data: agents,
        opacity: 1
      })
    ];

    return (
      <DeckGL {...viewport} layers={layers} onWebGLInitialized={this._initialize} />
    );
  }
}
