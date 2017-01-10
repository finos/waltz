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


function setup() {
    const module = angular.module('waltz.physical.flows', []);

    module
        .config(require('./routes'));

    module
        .service('PhysicalFlowStore', require('./service/physical-flow-store'));

    module
        .component('waltzPhysicalFlowOverview', require('./components/overview/physical-flow-overview'))
        .component('waltzPhysicalFlowEditOverview', require('./components/register/physical-flow-edit-overview'))
        .component("waltzPhysicalFlowEditSpecification", require('./components/register/physical-flow-edit-specification'))
        .component('waltzPhysicalFlowTable', require('./components/flow-table/physical-flow-table'))
        .component('waltzPhysicalFlowEditTargetEntity', require('./components/edit-target-entity/physical-flow-edit-target-entity'))
        .component('waltzPhysicalFlowExportButtons', require('./components/export-buttons/physical-flow-export-buttons'))
        .component('waltzPhysicalFlowAttributeEditor', require('./components/attribute-editor/physical-flow-attribute-editor'));

    return module.name;
}


export default setup;
