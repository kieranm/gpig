import React, {Component} from 'react';
import {Icon} from 'semantic-ui-react';

export default class Branding extends Component {

    render() {
        return (
            <nav>
                <div>
                    O<small>cean</small>X
                </div>
                <span>Shipping Simulation Engine</span>
                <div>
                    <span>
                        <Icon name='cubes' /> {this.props.totalCargo}
                    </span>
                    <span>
                        <Icon name='dashboard' /> {this.props.totalThroughput}
                    </span>
                    <span>
                        <Icon name='clock' /> {this.props.averageWaitTime}
                    </span>
                </div>
            </nav>
        );
    }
}