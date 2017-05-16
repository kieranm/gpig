import React, {Component} from 'react';
import {Segment, Header, Divider, Button} from 'semantic-ui-react';

export default class ControlPanel extends Component {
    render() {
        return(
            <Segment color={"teal"} className={"control panel"}>
                <Header as="h1">
                    Controls
                </Header>
                <Divider horizontal>Simulation Speed</Divider>
                <Button.Group fluid>
                    <Button color={"teal"}>1x</Button>
                    <Button color={"teal"}>10x</Button>
                    <Button color={"teal"}>20x</Button>
                </Button.Group>

                <Divider horizontal>Mode</Divider>
                <Button.Group fluid>
                    <Button color={"teal"}>Legacy</Button>
                    <Button color={"teal"}>OceanX</Button>
                </Button.Group>

                <Divider horizontal>Presets</Divider>
                <Button fluid color={"teal"}>Coastal Port</Button>
                <Button fluid color={"teal"}>Offshore Smart Port</Button>
                <Button fluid color={"teal"}>Weather Avoidance</Button>
                <Button fluid color={"teal"}>Road Traffic</Button>
                <Button fluid color={"teal"}>Global Movements</Button>

                <Divider horizontal>Map Style</Divider>
                <Button.Group fluid>
                    <Button color={"teal"}>Sat</Button>
                    <Button color={"teal"}>Light</Button>
                    <Button color={"teal"}>Dark</Button>
                </Button.Group>

            </Segment>
        )
    }
}