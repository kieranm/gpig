import React, {Component} from 'react';
import DeckGL, {PolygonLayer} from 'deck.gl';

import ShipsLayer from './ships-layer';

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
        const {viewport, ships, coastal_ports} = this.props;

        const layers = [
            new ShipsLayer({
                id: 'ships',
                data: ships,
                opacity: 1
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
