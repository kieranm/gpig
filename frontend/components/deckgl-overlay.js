import React, {Component} from 'react';
import DeckGL from 'deck.gl';

import ShipsLayer from './ships-layer';

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
    const {viewport, agents, time} = this.props;

    if (!agents) {
      return null;
    }

    const layers = [
      new ShipsLayer({
        id: 'ships',
        data: agents,
        getColor: d => [23, 184, 190],
        opacity: 0.4
      })
    ];

    return (
      <DeckGL {...viewport} layers={layers} onWebGLInitialized={this._initialize} />
    );
  }
}
