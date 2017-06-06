import React, {Component} from 'react';
import MapGL from 'react-map-gl';
import DeckGLOverlay from './deckgl-overlay';
import Branding from './branding'
import ControlPanel from './control_panel'

// Set your mapbox token here
const MAPBOX_TOKEN =
    "pk.eyJ1IjoibWF0emlwYW4iLCJhIjoiY2oya2VjZmIxMDAxZTJxcGhuajczMTdhMiJ9.G_uHqGV9YXlCtrTP4BQeVA"; // eslint-disable-line
const bar_width = 0.008;
const bar_height = 0.005;
const padding_width = bar_width/3;
const padding_height = bar_height/3;
const startingSpeed = 1;

const BAR_HEIGHT_FACTOR = 10;

export default class Root extends Component {

    constructor(props) {
        super(props);
        this.state = {
            viewport: {
                longitude:  -3.87085,
                latitude: 53.0049,
                zoom: 6,
                maxZoom: 16,
                pitch: 45,
                bearing: 0,
                width: 500,
                height: 500
            },
            mode: "legacy",
            mapStyle: "satellite",
            autonomousShips: {},
            freightShips: {},
            aircraft: {},
            ships: {},
            portBases: [],
            northEastPortBars: [],
            southEastPortBars: [],
            southWestPortBars: [],
            northWestPortBars: [],
            hoveredFeature: null,
            time: 0,
            totalCargo: 0,
            totalThroughput: 0,
            averageWaitTime: 0
        };

        this.connection = new WebSocket('ws://localhost:4567/sim');
    }

    processShip(ship, ships) {
        // Update position of freight ship agent or move it on the map
        var new_ship = null;

        if (ship.id in ships) {
            var target_ship = ships[ship.id];

            var positions = target_ship.positions.slice();
            const difference = positions[0].longitude - ship.coordinates.longitude;

            if ((ship.coordinates.longitude < 0 && difference > 300) || (ship.coordinates.longitude > 0 && difference < -300)) {
                positions = [ship.coordinates];
            } else {
                positions.unshift(ship.coordinates);
                if (positions.length > 60) {
                    positions.pop();
                }
            }

            new_ship = {
                id: ship.id,
                positions: positions,
                utilization: ship.load/ship.capacity
            };
        } else {
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

        var decription = "";
        var debug = Object.entries(port.debug);

        for(var i=0; i< debug.length; i++) {
            decription += debug[i][0] + ": " + debug[i][1] + '<br/>';
        }

        return {
            id: port.id,
            title: port.name,
            description: decription,
            height: 20 * 10,
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

        var title = port.statistics[direction].name;
        var description = port.statistics[direction].value;
        var height = port.statistics[direction].value;

        if (direction === "NE") {
            polygon = [
                [longitude + padding_width, latitude + padding_height],
                [longitude + padding_width, latitude + (2*bar_height) + padding_height],
                [longitude + (2*bar_width) + padding_width, latitude + (2*bar_height) + padding_height],
                [longitude + (2*bar_width) + padding_width, latitude + padding_height],
                [longitude + padding_width, latitude + padding_height],
            ];

        } else if (direction == "SE") {
            polygon = [
                [longitude + padding_width, latitude - padding_height],
                [longitude + (2*bar_width) + padding_width, latitude - padding_height],
                [longitude + (2*bar_width) + padding_width, latitude - (2*bar_height) - padding_height],
                [longitude + padding_width, latitude - (2*bar_height) - padding_height],
                [longitude + padding_width, latitude - padding_height],
            ];

            height = height * 20;

        } else if (direction == "SW") {
            polygon = [
                [longitude - padding_width, latitude - padding_height],
                [longitude - padding_width, latitude - (2*bar_height) - padding_height],
                [longitude - (2*bar_width) - padding_width, latitude - (2*bar_height) - padding_height],
                [longitude - (2*bar_width) - padding_width, latitude - padding_height],
                [longitude - padding_width, latitude - padding_height],
            ];

            height = height * 20;

        } else {
            polygon = [
                [longitude - padding_width, latitude + padding_height],
                [longitude - (2*bar_width) - padding_width, latitude + padding_height],
                [longitude - (2*bar_width) - padding_width, latitude + (2*bar_height) + padding_height],
                [longitude - padding_width, latitude + (2*bar_height) + padding_height],
                [longitude - padding_width, latitude + padding_height],
            ];
        }

        return {
            id: port.id,
            title: title,
            description: description,
            height: ((height+25) * BAR_HEIGHT_FACTOR),
            polygon: polygon
        };
    }


    _processAgents(d) {
        // Parse the update from the backend
        var freightShips = {};
        var autonomousShips = {};
        var aircraft = {};
        var portBases = [];
        var northEastPortBars = [];
        var southEastPortBars = [];
        var southWestPortBars = [];
        var northWestPortBars = [];

        for (var i = 0; i < d.agents.length; i++) {
            var agent = d.agents[i];

            if (agent.type === "FREIGHT_SHIP") {
                freightShips[agent.id] = this.processShip(agent, this.state.freightShips);
            } else if (agent.type === "SMART_SHIP") {
                autonomousShips[agent.id] = this.processShip(agent, this.state.autonomousShips);
            } else if (agent.type === "AIRCRAFT") {
                aircraft[agent.id] = this.processShip(agent, this.state.aircraft);
            } else if (agent.type === "LAND_PORT" || agent.type === "SMART_PORT") {
                portBases.push(this.portBase(agent));
                northEastPortBars.push(this.portBar("NE", agent));
                southEastPortBars.push(this.portBar("SE", agent));
                southWestPortBars.push(this.portBar("SW", agent));
                northWestPortBars.push(this.portBar("NW", agent));
            }
        }

        this.setState({
            autonomousShips,
            freightShips,
            aircraft,
            portBases,
            northEastPortBars,
            southEastPortBars,
            southWestPortBars,
            northWestPortBars
        });

    }

    componentDidMount() {
        this.connection.onmessage = this._onMessage.bind(this);
        this.connection.onopen = this._onOpen.bind(this);

        window.addEventListener('resize', this._resize.bind(this));
        this._resize();
        this._animate();

    }

    componentWillUnmount() {
        if (this._animation) {
            window.cancelAnimationFrame(this._animationFrame);
        }
    }

    _onOpen (e) {
        this.connection.send(JSON.stringify({
            message_type: "start",
            message_data: {
                speed_multiplier: startingSpeed,
                mode: this.state.mode
            }
        }));
    }

    _onMessage(e) {
        var d = JSON.parse(e.data);

        if(d.message_type == "update") {
            this._processAgents(d.message_body);
            if(d.message_body.statistics) {
                this._updateGlobalStats(d.message_body.statistics);
            }
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

    _showScenario (scenario) {
        if(scenario == "coastal port") {
            this.setState({
                viewport: {
                    ...this.state.viewport,
                    latitude: 53.459204,
                    longitude: -3.031712,
                    zoom: 10,
                    pitch: 45,
                    bearing: 25
                }
            });
        }

        if(scenario == "offshore port") {
            this.setState({
                viewport: {
                    ...this.state.viewport,
                    latitude: 53.490970,
                    longitude: -4.916830,
                    zoom: 8,
                    pitch: 45,
                    bearing: 25
                }
            });
        }

        if(scenario == "weather avoidance") {

        }

        if(scenario == "humanitarian aid") {
            this.setState({
                viewport: {
                    ...this.state.viewport,
                    latitude: 19.117760,
                    longitude: -72.828369,
                    zoom: 8,
                    pitch: 0,
                    bearing: 0
                }
            });
        }

        if(scenario == "global movements") {
            this.setState({
                viewport: {
                    ...this.state.viewport,
                    latitude: 20.210656,
                    longitude: 0,
                    zoom: 1.5,
                    pitch: 0,
                    bearing: 0
                }
            });
        }
    }

    _changeMode(mode) {
        this.connection.send(JSON.stringify({
            message_type: "change_mode",
            message_data: mode
        }));
    }

    _changeSpeed(ratio) {
        this.connection.send(JSON.stringify({
            message_type: "change_speed",
            message_data: ratio
        }));
    }

    _onHover({x, y, object}) {
        this.setState({hoveredFeature: object, x, y});
    }

    _renderTooltip() {
        const {x, y, hoveredFeature} = this.state;
        return hoveredFeature && (
                <div className="tooltip" style={{top: y, left: x}}>
                    <div><b>{hoveredFeature.title}</b></div>
                    <div dangerouslySetInnerHTML={{__html: hoveredFeature.description}}/>
                </div>
            );
    }

    _updateGlobalStats({total_cargo_delivered, total_throughput, average_waiting_time}) {
        var state = {
            totalCargo: total_cargo_delivered,
            totalThroughput: total_throughput,
            averageWaitTime: average_waiting_time
        };
        this.setState(state);
    }

    render() {
        const {
            viewport,
            autonomousShips,
            freightShips,
            aircraft,
            mode,
            portBases,
            northEastPortBars,
            southEastPortBars,
            southWestPortBars,
            northWestPortBars,
            time,
            mapStyle,
            totalCargo,
            totalThroughput,
            averageWaitTime
        } = this.state;

        var actualMapStyleUrl = "";

        if (mapStyle == "dark") {
            actualMapStyleUrl = "mapbox://styles/matzipan/cj2t849hk001c2rpeljwuiji9";
        } else {
            actualMapStyleUrl = "mapbox://styles/mapbox/"+ mapStyle +"-v9";
        }

        return (
            <div>
                <Branding
                    totalCargo={totalCargo}
                    totalThroughput={totalThroughput}
                    averageWaitTime={averageWaitTime} />
                <ControlPanel
                    mapStyleChangeCallback={this._changeMapStyle.bind(this)}
                    showScenarioCallback={this._showScenario.bind(this)}
                    changeModeCallback={this._changeMode.bind(this)}
                    changeSpeedCallback={this._changeSpeed.bind(this)}
                    startingSpeed={startingSpeed}
                    startingMode={mode}
                    startingScenario="coastal ports"
                    startingMapStyle={mapStyle}
                />
                <MapGL
                    {...viewport}
                    mapStyle={actualMapStyleUrl}
                    perspectiveEnabled={true}
                    onChangeViewport={this._onChangeViewport.bind(this)}
                    mapboxApiAccessToken={MAPBOX_TOKEN}>
                    <DeckGLOverlay viewport={viewport}
                                   autonomousShips={autonomousShips}
                                   freightShips={freightShips}
                                   aircraft={aircraft}
                                   mode={mode}
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