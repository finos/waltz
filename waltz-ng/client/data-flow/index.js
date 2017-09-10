
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import angular from 'angular';

import {registerComponents} from '../common/module-utils';
import * as AppAuthorityPanel from './components/app-authority-panel/app-authority-panel';
import * as DataFlowSection from './components/data-flow-section/data-flow-section';


function setup() {
    const module = angular.module('waltz.data-flow', []);

    registerComponents(module, [ AppAuthorityPanel, DataFlowSection ]);

    return module.name;
}


export default setup;
