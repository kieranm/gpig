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
            time: 0
        };

        this.connection = new WebSocket('ws://localhost:4567/sim');

    }

    freightShip(ship) {
        // Update position of freight ship agent or move it on the map
        var self = this;
        var new_ship;
        var found = false;

        for (var j = 0; j < self.state.ships.length; j++) {
            var target_ship = self.state.ships[j];

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

    coastalPort(agent) {
        // Add coastal port or change its load

    }

    processAgents(d) {
        // Parse the update from the backend

        var self = this;
        var ships = [];
        var coastal_ports = [];

        for (var i = 0; i < d.agents.length; i++) {
            var agent = d.agents[i];

            if (agent.type === "FREIGHT_SHIP") {
                ships.push(self.freightShip(agent));
            } else if (agent.type === "COASTAL_PORT") {
                coastal_ports.push(self.coastalPort(agent));
            }
        }

        self.setState({
            ships: ships,
            coastal_ports: coastal_ports
        });

    }

    componentDidMount() {
        var self = this;
        this.connection.onmessage = function(e) {
            var d = JSON.parse(e.data);
            self.processAgents(d);
            console.log('process agents');
        };

        window.addEventListener('resize', this._resize.bind(this));
        this._resize();
        this._animate();

    }

    componentWillUnmount() {
        if (this._animation) {
            window.cancelAnimationFrame(this._animationFrame);
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
        const {viewport, ships, time} = this.state;

        return (
            <div>
                <Branding/>
                <ControlPanel/>
                <MapGL
                    {...viewport}
                    mapStyle="mapbox://styles/mapbox/satellite-v9"
                    perspectiveEnabled={true}
                    onChangeViewport={this._onChangeViewport.bind(this)}
                    mapboxApiAccessToken={MAPBOX_TOKEN}>
                    <DeckGLOverlay viewport={viewport}
                                   ships={ships}
                                   time={time}
                    />
                </MapGL>
            </div>
        );
    }

}