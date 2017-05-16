/* global window,document */
import React, {Component} from 'react';
import {render} from 'react-dom';


import Branding from './components/branding'
import ControlPanel from './components/control_panel'
import Root from './components/root'

render(<Branding />, document.body.appendChild(document.createElement('div')));
render(<ControlPanel />, document.body.appendChild(document.createElement('div')));
render(<Root />, document.body.appendChild(document.createElement('div')));
