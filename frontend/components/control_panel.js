import React, {Component} from 'react';
import {Segment, Header, Divider, Button, Accordion, AccordionTitle, AccordionContent} from 'semantic-ui-react';

export default class ControlPanel extends Component {
    constructor(props) {
        super(props);
        // Using underscore naming scheme here to keep interface convention with the backend
        this.state = {
            speedMultiplier: this.props.startingSpeed,
            mode: this.props.startingMode,
            scenario: this.props.startingScenario,
            mapStyle: this.props.startingMapStyle
        };
    }

    changeSpeed(ratio) {
        this.setState({
            speedMultiplier: ratio
        }, () => {
            this.props.changeSpeedCallback(ratio);
        });
    }

    changeMapStyle(newStyle) {
        this.setState({
            mapStyle: newStyle
        }, () => {
            this.props.mapStyleChangeCallback(newStyle);
        });
    }

    showScenario(newScenario) {
        this.setState({
            scenario: newScenario
        }, () => {
            this.props.showScenarioCallback(newScenario);
        });
    }

    changeMode(newMode) {
        this.setState({
            mode: newMode
        }, () => {
            this.props.changeModeCallback(newMode);
        });
    }

    render() {
        const { speedMultiplier, mapStyle, mode } = this.state;

        return(
            <Segment color="teal" className="control panel">

                <Accordion>
                    <AccordionTitle>
                        <Header as="h1">
                            Controls
                        </Header>
                    </AccordionTitle>
                    <AccordionContent>
                        <Divider horizontal>Simulation Speed</Divider>
                        <Button.Group fluid>
                            <Button onClick={(e) => this.changeSpeed(1)} color="teal" active={speedMultiplier == 1}>1x</Button>
                            <Button onClick={(e) => this.changeSpeed(10)} color="teal" active={speedMultiplier == 10}>10x</Button>
                            <Button onClick={(e) => this.changeSpeed(20)} color="teal" active={speedMultiplier == 20}>20x</Button>
                        </Button.Group>

                        <Divider horizontal>Mode</Divider>
                        <Button.Group fluid>
                            <Button color="teal" onClick={(e) => this.changeMode("legacy")} active={mode == "legacy"}>Legacy</Button>
                            <Button color="teal" onClick={(e) => this.changeMode("oceanx")} active={mode == "oceanx"}>OceanX</Button>
                        </Button.Group>

                        <Divider horizontal>Scenarios</Divider>
                        <Button fluid color="teal" onClick={(e) => this.showScenario("coastal port") }>Coastal Port</Button>
                        <Button fluid color="teal" onClick={(e) => this.showScenario("offshore port") }>Offshore Smart Port</Button>
                        <Button fluid color="teal" onClick={(e) => this.showScenario("weather avoidance") }>Weather Avoidance</Button>
                        <Button fluid color="teal" onClick={(e) => this.showScenario("road traffic") }>Road Traffic</Button>
                        <Button fluid color="teal" onClick={(e) => this.showScenario("global movements") }>Global Movements</Button>

                        <Divider horizontal>Map Style</Divider>
                        <Button.Group fluid>
                            <Button onClick={(e) => this.changeMapStyle("satellite")} color="teal" active={mapStyle == "satellite"}>Sat</Button>
                            <Button onClick={(e) => this.changeMapStyle("light")} color="teal" active={mapStyle == "light"}>Light</Button>
                            <Button onClick={(e) => this.changeMapStyle("dark")} color="teal" active={mapStyle == "dark"}>Dark</Button>
                        </Button.Group>

                    </AccordionContent>
                </Accordion>


            </Segment>
        )
    }
}