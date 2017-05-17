import React, {Component} from 'react';
import DeckGL, {PolygonLayer} from 'deck.gl';

import ShipsLayer from './ships-layer';

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
                getElevation: d => d.height
            })
        ];

        return (
            <DeckGL {...viewport} layers={layers} onWebGLInitialized={this._initialize}/>
        );
    }
}
