/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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

import {registerComponents, registerStore} from '../common/module-utils';
import * as PhysicalFlowStore from './service/physical-flow-store';
import PhysicalFlowEditor from './components/flow-editor/physical-flow-editor';
import PhysicalFlowCloneSelector from './components/register/clone/physical-flow-clone-selector';
import PhysicalFlowOverview from './components/overview/physical-flow-overview';
import PhysicalFlowEditOverview from './components/register/overview/physical-flow-edit-overview';
import PhysicalFlowEditTargetLogicalFlow from './components/register/edit-target-logical-flow/physical-flow-edit-target-logical-flow';
import PhysicalFlowEditSpecification from './components/register/edit-specification/physical-flow-edit-specification';
import PhysicalFlowAttributeEditor from './components/register/attribute-editor/physical-flow-attribute-editor';


function setup() {
    const module = angular.module('waltz.physical.flows', []);

    module
        .config(require('./routes'));

    registerStore(module, PhysicalFlowStore);

    module
        .component('waltzPhysicalFlowTable', require('./components/flow-table/physical-flow-table'))
        .component('waltzPhysicalFlowExportButtons', require('./components/export-buttons/physical-flow-export-buttons'));

    registerComponents(module, [
        PhysicalFlowCloneSelector,
        PhysicalFlowEditor,
        PhysicalFlowOverview,
        PhysicalFlowEditOverview,
        PhysicalFlowEditTargetLogicalFlow,
        PhysicalFlowEditSpecification,
        PhysicalFlowAttributeEditor
    ]);
    return module.name;
}


export default setup;
