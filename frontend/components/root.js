import React, {Component} from 'react';
import MapGL from 'react-map-gl';
import DeckGLOverlay from './deckgl-overlay';

import Branding from './branding'
import ControlPanel from './control_panel'

// Set your mapbox token here
const MAPBOX_TOKEN =
    "pk.eyJ1IjoibWF0emlwYW4iLCJhIjoiY2oya2VjZmIxMDAxZTJxcGhuajczMTdhMiJ9.G_uHqGV9YXlCtrTP4BQeVA"; // eslint-disable-line
const bar_width = 0.0008;
const bar_height = 0.0005;
const padding_width = bar_width/3;
const padding_height = bar_height/3;

export default class Root extends Component {

    constructor(props) {
        super(props);
        this.state = {
            viewport: {
                ...DeckGLOverlay.defaultViewport,
                width: 500,
                height: 500
            },
            mapStyle: "dark",
            ships: [],
            portBases: [],
            northEastPortBars: [],
            southEastPortBars: [],
            southWestPortBars: [],
            northWestPortBars: [],
            hoveredFeature: null,
            time: 0
        };

        this.connection = new WebSocket('ws://localhost:4567/sim');
    }

    freightShip(ship) {
        // Update position of freight ship agent or move it on the map
        var new_ship = null;

        for (var j = 0; j < this.state.ships.length; j++) {
            var target_ship = this.state.ships[j];

            if(ship.id == target_ship.id) {

                var positions = target_ship.positions.slice();

                positions.unshift(ship.coordinates);
                if (positions.length > 30) {
                    positions.pop();
                }

                new_ship = {
                    id: ship.id,
                    positions: positions,
                    utilization: ship.load/ship.capacity
                };
            }
        }

        if (new_ship == null) {
            new_ship = {
                id: ship.id,
                positions: [ship.coordinates],
                utilization: ship.load/ship.capacity

            };
        }

        return new_ship;
    }

    portBase(port) {
        var latitude = port.coordinates.latitude;
        var longitude = port.coordinates.longitude;

        for (var j = 0; j < this.state.portBases.length; j++) {
            var target_port = this.state.portBases[j];
            if (port.id == target_port.id) {
                return target_port;
            }
        }

        return {
            id: port.id,
            height: 20,
            polygon: [
                [longitude - (4*bar_width), latitude + (4*bar_height)],
                [longitude + (4*bar_width), latitude + (4*bar_height)],
                [longitude + (4*bar_width), latitude - (4*bar_height)],
                [longitude - (4*bar_width), latitude - (4*bar_height)],
                [longitude - (4*bar_width), latitude + (4*bar_height)],
            ]
        };

    }

    portBar(direction, port) {

        var polygon;
        var height;

        var latitude = port.coordinates.latitude;
        var longitude = port.coordinates.longitude;

        if (direction === "NE") {
            polygon = [
                [longitude + padding_width, latitude + padding_height],
                [longitude + padding_width, latitude + (2*bar_height) + padding_height],
                [longitude + (2*bar_width) + padding_width, latitude + (2*bar_height) + padding_height],
                [longitude + (2*bar_width) + padding_width, latitude + padding_height],
                [longitude + padding_width, latitude + padding_height],
            ];
            height = 1700;
        } else if (direction == "SE") {
            polygon = [
                [longitude + padding_width, latitude - padding_height],
                [longitude + (2*bar_width) + padding_width, latitude - padding_height],
                [longitude + (2*bar_width) + padding_width, latitude - (2*bar_height) - padding_height],
                [longitude + padding_width, latitude - (2*bar_height) - padding_height],
                [longitude + padding_width, latitude - padding_height],
            ];
            height = 1300
        } else if (direction == "SW") {
            polygon = [
                [longitude - padding_width, latitude - padding_height],
                [longitude - padding_width, latitude - (2*bar_height) - padding_height],
                [longitude - (2*bar_width) - padding_width, latitude - (2*bar_height) - padding_height],
                [longitude - (2*bar_width) - padding_width, latitude - padding_height],
                [longitude - padding_width, latitude - padding_height],
            ];
            height = 1000;
        } else {
            polygon = [
                [longitude - padding_width, latitude + padding_height],
                [longitude - (2*bar_width) - padding_width, latitude + padding_height],
                [longitude - (2*bar_width) - padding_width, latitude + (2*bar_height) + padding_height],
                [longitude - padding_width, latitude + (2*bar_height) + padding_height],
                [longitude - padding_width, latitude + padding_height],
            ];
            height = 1500;
        }

        return {
            id: port.id,
            height: height,
            polygon: polygon
        };
    }


    processAgents(d) {
        // Parse the update from the backend

        var ships = [];
        var portBases = [];
        var northEastPortBars = [];
        var southEastPortBars = [];
        var southWestPortBars = [];
        var northWestPortBars = [];

        for (var i = 0; i < d.agents.length; i++) {
            var agent = d.agents[i];

            if (agent.type === "FREIGHT_SHIP") {
                ships.push(this.freightShip(agent));
            } else if (agent.type === "LAND_PORT") {
                portBases.push(this.portBase(agent));
                northEastPortBars.push(this.portBar("NE", agent));
                southEastPortBars.push(this.portBar("SE", agent));
                southWestPortBars.push(this.portBar("SW", agent));
                northWestPortBars.push(this.portBar("NW", agent));
            }
        }

        this.setState({
            ships: ships,
            portBases: portBases,
            northEastPortBars: northEastPortBars,
            southEastPortBars: southEastPortBars,
            southWestPortBars: southWestPortBars,
            northWestPortBars: northWestPortBars
        });

    }

    componentDidMount() {
        this.connection.onmessage = this._onMessage.bind(this);

        window.addEventListener('resize', this._resize.bind(this));
        this._resize();
        this._animate();

    }

    componentWillUnmount() {
        if (this._animation) {
            window.cancelAnimationFrame(this._animationFrame);
        }
    }

    _onMessage(e) {
        var d = JSON.parse(e.data);

        if(d.message_type == "update") {
            this.processAgents(d.message_body);
        }
    }

    _animate() {
        this.setState({
            time: Date.now()
        });

        this._animationFrame = window.requestAnimationFrame(this._animate.bind(this));
    }

    _resize() {
        this._onChangeViewport({
            width: window.innerWidth,
            height: window.innerHeight
        });
    }

    _onChangeViewport(viewport) {
        this.setState({
            viewport: {...this.state.viewport, ...viewport}
        });
    }

    _changeMapStyle(style) {
        this.setState({mapStyle: style});
    }

    _onHover({x, y, object}) {
        console.log(object);
        this.setState({hoveredFeature: object, x, y});
    }

    _renderTooltip() {
        const {x, y, hoveredFeature} = this.state;
        return hoveredFeature && (
                <div className="tooltip" style={{top: y, left: x}}>
                    <div><b>${hoveredFeature.properties.name}</b></div>

                </div>
            );
    }

    render() {
        const {
            viewport,
            ships,
            portBases,
            northEastPortBars,
            southEastPortBars,
            southWestPortBars,
            northWestPortBars,
            time
        } = this.state;

        var {mapStyle} = this.state;
        var actualMapStyleUrl = "";

        if (mapStyle == "dark") {
            actualMapStyleUrl = "mapbox://styles/matzipan/cj2t849hk001c2rpeljwuiji9";
        } else {
            actualMapStyleUrl = "mapbox://styles/mapbox/"+ mapStyle +"-v9";
        }

        return (
            <div>
                <Branding/>
                <ControlPanel
                    connection={this.connection}
                    mapStyleChangeCallback={this._changeMapStyle.bind(this)}
                    activeMapStyle={mapStyle}
                />
                <MapGL
                    {...viewport}
                    mapStyle={actualMapStyleUrl}
                    perspectiveEnabled={true}
                    onChangeViewport={this._onChangeViewport.bind(this)}
                    mapboxApiAccessToken={MAPBOX_TOKEN}>
                    <DeckGLOverlay viewport={viewport}
                                   ships={ships}
                                   portBases={portBases}
                                   northEastPortBars={northEastPortBars}
                                   southEastPortBars={southEastPortBars}
                                   southWestPortBars={southWestPortBars}
                                   northWestPortBars={northWestPortBars}
                                   time={time}
                                   onHover={this._onHover.bind(this)}
                    />
                    {this._renderTooltip()}
                </MapGL>
            </div>
        );
    }

}