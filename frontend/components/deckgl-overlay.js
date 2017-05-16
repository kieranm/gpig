import React, {Component} from 'react';
import DeckGL, {PolygonLayer} from 'deck.gl';

import ShipsLayer from './ships-layer';

const LIGHT_SETTINGS = {
    lightsPosition: [-2.991573, 53.408371, 30000],
    ambientRatio: 0.05,
    diffuseRatio: 0.6,
    specularRatio: 0.8,
    lightsStrength: [2.0, 0.0, 0.0, 0.0],
    numberOfLights: 1
};

export default class DeckGLOverlay extends Component {

    static get defaultViewport() {
        return {
            longitude: -2.991573,
            latitude: 53.408371,
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
        const {viewport, ships, coastal_ports, time} = this.props;

        const layers = [
            new ShipsLayer({
                id: 'ships',
                data: ships,
                getColor: d => [23, 184, 190],
                opacity: 0.4
            }),

            new PolygonLayer({
                id: 'coastal_ports',
                data: coastal_ports,
                getFillColor: d => [23, 184, 190],
                extruded: true,
                filled: true,
                lightSettings: LIGHT_SETTINGS
            })
        ];

        return (
            <DeckGL {...viewport} layers={layers} onWebGLInitialized={this._initialize}/>
        );
    }
}
