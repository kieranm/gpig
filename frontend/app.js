/* global window,document */
import React, {Component} from 'react';
import {render} from 'react-dom';

import Root from './components/root'

render(<Root />, document.body.appendChild(document.createElement('div')));
