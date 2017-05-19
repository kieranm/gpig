import React, {Component} from 'react';
import {Segment, Header, Divider, Button} from 'semantic-ui-react';

export default class ControlPanel extends Component {
    constructor(props) {
        super(props);
        // Using underscore naming scheme here to keep interface convention with the backend
        this.state = {
            speed_multiplier: 1,
            map_style: this.props.activeMapStyle
        };
    }

    changeSpeed(ratio) {
        this.setState({
            speed_multiplier: ratio
        }, this.sendState);
    }

    changeMapStyle(newStyle) {
        this.props.mapStyleChangeCallback(newStyle);
    }

    sendState() {
        this.props.connection.send(JSON.stringify({
            message_type: "settings",
            message_data: this.state
        }));
    }

    render() {
        const {speed_multiplier, map_style} = this.state;

        return(
            <Segment color="teal" className="control panel">
                <Header as="h1">
                    Controls
                </Header>
                <Divider horizontal>Simulation Speed</Divider>
                <Button.Group fluid>
                    <Button onClick={(e) => this.changeSpeed(1)} color="teal" active={speed_multiplier == 1}>1x</Button>
                    <Button onClick={(e) => this.changeSpeed(10)} color="teal" active={speed_multiplier == 10}>10x</Button>
                    <Button onClick={(e) => this.changeSpeed(20)} color="teal" active={speed_multiplier == 20}>20x</Button>
                </Button.Group>

                <Divider horizontal>Mode</Divider>
                <Button.Group fluid>
                    <Button color="teal">Legacy</Button>
                    <Button color="teal">OceanX</Button>
                </Button.Group>

                <Divider horizontal>Presets</Divider>
                <Button fluid color="teal">Coastal Port</Button>
                <Button fluid color="teal">Offshore Smart Port</Button>
                <Button fluid color="teal">Weather Avoidance</Button>
                <Button fluid color="teal">Road Traffic</Button>
                <Button fluid color="teal">Global Movements</Button>

                <Divider horizontal>Map Style</Divider>
                <Button.Group fluid>
                    <Button onClick={(e) => this.changeMapStyle("satellite")} color="teal" active={map_style == "satellite"}>Sat</Button>
                    <Button onClick={(e) => this.changeMapStyle("light")} color="teal" active={map_style == "light"}>Light</Button>
                    <Button onClick={(e) => this.changeMapStyle("dark")} color="teal" active={map_style == "dark"}>Dark</Button>
                </Button.Group>

            </Segment>
        )
    }
}