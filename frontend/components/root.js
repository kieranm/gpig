import React, {Component} from 'react';
import MapGL from 'react-map-gl';
import DeckGLOverlay from './deckgl-overlay';

import Branding from './branding'
import ControlPanel from './control_panel'

// Set your mapbox token here
const MAPBOX_TOKEN =
    "pk.eyJ1IjoibWF0emlwYW4iLCJhIjoiY2oya2VjZmIxMDAxZTJxcGhuajczMTdhMiJ9.G_uHqGV9YXlCtrTP4BQeVA"; // eslint-disable-line

export default class Root extends Component {

    constructor(props) {
        super(props);
        this.state = {
            viewport: {
                ...DeckGLOverlay.defaultViewport,
                width: 500,
                height: 500
            },
            ships: [],
            coastal_ports: [],
            time: 0
        };

        this.connection = new WebSocket('ws://localhost:4567/sim');

    }

    freightShip(ship) {
        // Update position of freight ship agent or move it on the map
        var new_ship;
        var found = false;

        for (var j = 0; j < this.state.ships.length; j++) {
            var target_ship = this.state.ships[j];

            if(ship.id == target_ship.id) {
                found = true;

                var positions = target_ship.positions.slice();

                positions.unshift(ship.coordinates);
                if (positions.length > 30) {
                    positions.pop();
                }

                new_ship = {
                    id: ship.id,
                    positions: positions
                };
            }
        }

        if (found == false) {
            new_ship = {
                id: ship.id,
                positions: [ship.coordinates]
            };
        }

        return new_ship;
    }

    coastalPort(port) {
        // Add coastal port or change its load
        var new_port;
        var found = false;

        const bar_width = 0.001;
        const bar_height = 0.001;
        const initial_port_height = 500;

        var latitude = port.coordinates.latitude;
        var longitude = port.coordinates.longitude;

        for (var j = 0; j < this.state.coastal_ports.length; j++) {
            var target_port = this.state.coastal_ports[j];
            if (port.id == target_port.id) {
                found = true;

                var height;
                if(target_port.height < 1000) {
                    height = target_port.height + 10;
                } else {
                    height = 0;
                }

                new_port = {
                    id: port.id,
                    height: height,
                    polygon: [
                        [longitude - bar_width, latitude + bar_height],
                        [longitude + bar_width, latitude + bar_height],
                        [longitude + bar_width, latitude - bar_height],
                        [longitude - bar_width, latitude - bar_height],
                        [longitude - bar_width, latitude + bar_height],
                    ]
                };
            }
        }

        if(!found) {
            new_port = {
                id: port.id,
                height: initial_port_height,
                polygon: [
                    [longitude - bar_width, latitude + bar_height],
                    [longitude + bar_width, latitude + bar_height],
                    [longitude + bar_width, latitude - bar_height],
                    [longitude - bar_width, latitude - bar_height],
                    [longitude - bar_width, latitude + bar_height],
                ]
            };
        }

        return new_port;
    }

    processAgents(d) {
        // Parse the update from the backend

        var ships = [];
        var coastal_ports = [];

        for (var i = 0; i < d.agents.length; i++) {
            var agent = d.agents[i];

            if (agent.type === "FREIGHT_SHIP") {
                ships.push(this.freightShip(agent));
            } else if (agent.type === "LAND_PORT") {
                coastal_ports.push(this.coastalPort(agent));
            }
        }

        this.setState({
            ships: ships,
            coastal_ports: coastal_ports
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

    render() {
        const {viewport, ships, coastal_ports, time} = this.state;

        return (
            <div>
                <Branding/>
                <ControlPanel/>
                <MapGL
                    {...viewport}
                    mapStyle="mapbox://styles/mapbox/dark-v9"
                    perspectiveEnabled={true}
                    onChangeViewport={this._onChangeViewport.bind(this)}
                    mapboxApiAccessToken={MAPBOX_TOKEN}>
                    <DeckGLOverlay viewport={viewport}
                                   ships={ships}
                                   coastal_ports={coastal_ports}
                                   time={time}
                    />
                </MapGL>
            </div>
        );
    }

}