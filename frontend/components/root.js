import React, {Component} from 'react';
import MapGL from 'react-map-gl';
import DeckGLOverlay from '../deckgl-overlay.js';

import {json as requestJson} from 'd3-request';

import Branding from './branding'
import ControlPanel from './control_panel'

// Set your mapbox token here
const MAPBOX_TOKEN = "pk.eyJ1IjoibWF0emlwYW4iLCJhIjoiY2oya2VjZmIxMDAxZTJxcGhuajczMTdhMiJ9.G_uHqGV9YXlCtrTP4BQeVA"; // eslint-disable-line

export default class Root extends Component {

    constructor(props) {
        super(props);
        this.state = {
            viewport: {
                ...DeckGLOverlay.defaultViewport,
                width: 500,
                height: 500
            },
            agents: [],
            time: 0
        };

        this.connection = new WebSocket('ws://localhost:4567/sim');

    }

    componentDidMount() {
        var self = this;
        this.connection.onmessage = function(e) {
            var d = JSON.parse(e.data);
            var new_agents = [];

            for (var i = 0; i < d.agents.length; i++) {
                var agent = d.agents[i];
                var found = false;

                if (agent.type != "FREIGHT_SHIP") {
                    continue;
                }

                for (var j = 0; j < self.state.agents.length; j++) {
                    var target_agent = self.state.agents[j];

                    if(agent.id == target_agent.id) {
                        found = true;

                        var positions = target_agent.positions.slice();

                        positions.unshift(agent.coordinates);
                        if (positions.length > 30) { // Hardcoding this because I'm lazy and I can't be asked
                            positions.pop();
                        }

                        new_agents.push({
                            id: agent.id,
                            positions: positions
                        });
                    }
                }

                if (found == false) {
                    new_agents.push({
                        id: agent.id,
                        positions: [agent.coordinates]
                    });
                }
            }

            self.setState({
                agents: new_agents
            });
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
        const {viewport, agents, time} = this.state;

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
                                   agents={agents}
                                   time={time}
                    />
                </MapGL>
            </div>
        );
    }

}