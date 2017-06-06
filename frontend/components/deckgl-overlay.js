import React, {Component} from 'react';
import DeckGL, {PolygonLayer} from 'deck.gl';

import ShipsLayer from './ships-layer';

const LIGHT_SETTINGS = {
    lightsPosition: [0, 53.32463, 300000],
    ambientRatio: 0.5,
    diffuseRatio: 0.6,
    specularRatio: 0.8,
    lightsStrength: [1.35, 0.0, 0.0, 0.0],
    numberOfLights: 1
};

export default class DeckGLOverlay extends Component {

    _initialize(gl) {
        gl.enable(gl.DEPTH_TEST);
        gl.depthFunc(gl.LEQUAL);
    }

    render() {
        const {
            viewport,
            autonomousShips,
            freightShips,
            portBases,
            northEastPortBars,
            southEastPortBars,
            southWestPortBars,
            northWestPortBars,
            weather
        } = this.props;

        const layers = [

            // weather
            new PolygonLayer({
                id: 'weather',
                data: weather,
                getFillColor: d =>  d.colour,
                extruded: true,
                filled: true,
                getElevation: d => d.height,
                lightSettings: LIGHT_SETTINGS,
                pickable: false
            }),

            // Ship spark-line layer
            new ShipsLayer({
                id: 'ships',
                data: autonomousShips,
                lineWidth: 4,
                lengthMultiplier: 1,
                opacity: 1
            }),
            new ShipsLayer({
                id: 'ships',
                data: freightShips,
                lineWidth: this.props.mode == "legacy" ? 4 : 8,
                lengthMultiplier: this.props.mode == "legacy" ? 1 : 2,
                opacity: 1
            }),

            // Port Bases Layer
            new PolygonLayer({
                id: 'portBases',
                data: portBases,
                getFillColor: d => [150, 150, 150],
                extruded: true,
                filled: true,
                getElevation: d => d.height,
                lightSettings: LIGHT_SETTINGS,
                pickable: Boolean(this.props.onHover),
                onHover: this.props.onHover
            }),

            // North East Port Bars
            new PolygonLayer({
                id: 'northEastPortBars',
                data: northEastPortBars,
                getFillColor: d => [202, 0, 32],
                extruded: true,
                filled: true,
                getElevation: d => d.height,
                lightSettings: LIGHT_SETTINGS,
                pickable: Boolean(this.props.onHover),
                onHover: this.props.onHover
            }),

            // South East Port Bars
            new PolygonLayer({
                id: 'southEastPortBars',
                data: southEastPortBars,
                getFillColor: d => [5, 113, 176],
                extruded: true,
                filled: true,
                getElevation: d => d.height,
                lightSettings: LIGHT_SETTINGS,
                pickable: Boolean(this.props.onHover),
                onHover: this.props.onHover
            }),

            // South West Port Bars
            new PolygonLayer({
                id: 'southWestPortBars',
                data: southWestPortBars,
                getFillColor: d => [166, 217, 106],
                extruded: true,
                filled: true,
                getElevation: d => d.height,
                lightSettings: LIGHT_SETTINGS,
                pickable: Boolean(this.props.onHover),
                onHover: this.props.onHover
            }),

            // North West Port Bars
            new PolygonLayer({
                id: 'northWestPortBars',
                data: northWestPortBars,
                getFillColor: d => [123, 50, 148],
                extruded: true,
                filled: true,
                getElevation: d => d.height,
                lightSettings: LIGHT_SETTINGS,
                pickable: Boolean(this.props.onHover),
                onHover: this.props.onHover
            })
        ];

        return (
            <DeckGL {...viewport} layers={layers} onWebGLInitialized={this._initialize}/>
        );
    }
}
