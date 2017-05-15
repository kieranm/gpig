/* global window,document */
import React, {Component} from 'react';
import {render} from 'react-dom';
import MapGL from 'react-map-gl';
import DeckGLOverlay from './deckgl-overlay.js';
import {Segment, Header, Divider, Button} from 'semantic-ui-react';

import {json as requestJson} from 'd3-request';

// Set your mapbox token here
const MAPBOX_TOKEN = "pk.eyJ1IjoibWF0emlwYW4iLCJhIjoiY2oya2VjZmIxMDAxZTJxcGhuajczMTdhMiJ9.G_uHqGV9YXlCtrTP4BQeVA"; // eslint-disable-line

class Branding extends Component {
    render() {
        return (
            <nav>
                <div>
                O<small>cean</small>X
                </div>
                <span>Shipping Simulation Engine</span>
            </nav>
        );
    }
}

class ControlPanel extends Component {
    render() {
        return(
            <Segment color={"orange"} className={"control panel"}>
                <Header as="h1">
                    Controls
                </Header>
                <Divider horizontal>Simulation Speed</Divider>
                <Button.Group fluid>
                    <Button color={"orange"} icon={"pause"}/>
                    <Button color={"orange"} >1x</Button>
                    <Button color={"orange"} >5x</Button>
                    <Button color={"orange"} >10x</Button>
                    <Button color={"orange"} >20x</Button>
                </Button.Group>

                <Divider horizontal>Mode</Divider>
                <Button.Group fluid>
                    <Button color={"orange"}>Legacy</Button>
                    <Button color={"orange"}>OceanX</Button>
                </Button.Group>

                <Divider horizontal>Presets</Divider>
                <Button fluid color={"orange"}>Coastal Port</Button>
                <Button fluid color={"orange"}>Offshore Smart Port</Button>
                <Button fluid color={"orange"}>Weather Avoidance</Button>
                <Button fluid color={"orange"}>Road Traffic</Button>
                <Button fluid color={"orange"}>Global Movements</Button>

                <Divider horizontal>Map Style</Divider>
                <Button.Group fluid>
                    <Button color={"orange"}>Satellite</Button>
                    <Button color={"orange"}>Light</Button>
                    <Button color={"orange"}>Dark</Button>
                </Button.Group>

            </Segment>
        )
    }
}

class Root extends Component {

    constructor(props) {
        super(props);
        this.state = {
            viewport: {
                ...DeckGLOverlay.defaultViewport,
                width: 500,
                height: 500
            },
            buildings: null,
            trips: null,
            time: 0
        };

        requestJson('./data/buildings.json', (error, response) => {
            if (!error) {
                this.setState({buildings: response});
            }
        });

        requestJson('./data/trips.json', (error, response) => {
            if (!error) {
                this.setState({trips: response});
            }
        });
    }

    componentDidMount() {
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
        const timestamp = Date.now();
        const loopLength = 14;
        const loopTime = 30000;

        this.setState({
            time: ((timestamp % loopTime) / loopTime) * loopLength
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
        const {viewport, buildings, trips, time} = this.state;

        return (
            <MapGL
                {...viewport}
                mapStyle="mapbox://styles/mapbox/satellite-v9"
                perspectiveEnabled={true}
                onChangeViewport={this._onChangeViewport.bind(this)}
                mapboxApiAccessToken={MAPBOX_TOKEN}>
                <DeckGLOverlay viewport={viewport}
                               buildings={buildings}
                               trips={trips}
                               trailLength={180}
                               time={time}
                />
            </MapGL>
        );
    }
}

render(<Branding />, document.body.appendChild(document.createElement('div')));
render(<ControlPanel />, document.body.appendChild(document.createElement('div')));
render(<Root />, document.body.appendChild(document.createElement('div')));
