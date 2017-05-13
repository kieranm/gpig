import React, {Component} from 'react';
import DeckGL, {PolygonLayer} from 'deck.gl';

import TripsLayer from './trips-layer';

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
    const {viewport, buildings, trips, trailLength, time} = this.props;

    if (!buildings || !trips) {
      return null;
    }

    const layers = [
      new TripsLayer({
        id: 'trips',
        data: trips,
        getPath: d => d.segments,
        getColor: d => d.vendor === 0 ? [253, 128, 93] : [23, 184, 190],
        opacity: 0.3,
        strokeWidth: 2,
        trailLength,
        currentTime: time
      }),
      new PolygonLayer({
        id: 'buildings',
        data: buildings,
        extruded: true,
        filled: true,
        opacity: 1,
        wireframe: false,
        fp64: true,
        getPolygon: f => f.polygon,
        getElevation: f => f.height,
        getFillColor: f => [74, 80, 87],
        lightSettings: LIGHT_SETTINGS
      })
    ];

    return (
      <DeckGL {...viewport} layers={layers} onWebGLInitialized={this._initialize} />
    );
  }
}
