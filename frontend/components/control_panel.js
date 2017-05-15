import React, {Component} from 'react';
import {Segment, Header, Divider, Button} from 'semantic-ui-react';

export default class ControlPanel extends Component {
    render() {
        return(
            <Segment color={"orange"} className={"control panel"}>
                <Header as="h1">
                    Controls
                </Header>
                <Divider horizontal>Simulation Speed</Divider>
                <Button.Group fluid>
                    <Button color={"orange"} icon={"pause"}/>
                    <Button color={"orange"}>1x</Button>
                    <Button color={"orange"}>5x</Button>
                    <Button color={"orange"}>10x</Button>
                    <Button color={"orange"}>20x</Button>
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