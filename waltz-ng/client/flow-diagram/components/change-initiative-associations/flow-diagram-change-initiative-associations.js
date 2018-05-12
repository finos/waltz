
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


import _ from 'lodash';

import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";

import template from './flow-diagram-change-initiative-associations.html';
import {mkSelectionOptions} from "../../../common/selector-utils";
import {toEntityRef} from "../../../common/entity-utils";


const bindings = {
    diagramId: '<',
    readOnly: '<'
};


const initialState = {
    associations: [],
    visibility: {
        add: false
    }
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadAssociations = () => {
        serviceBroker
            .loadViewData(
                CORE_API.ChangeInitiativeStore.findBySelector,
                [ mkSelectionOptions({ id: vm.diagramId, kind: 'FLOW_DIAGRAM' })],
                { force: true })
            .then(r => vm.associations = _.sortBy(r.data, 'name'));
    };


    vm.$onInit = () => {
        loadAssociations();
    };


    vm.onRemove = (c) => {
        serviceBroker
            .execute(
                CORE_API.FlowDiagramEntityStore.removeRelationship,
                [ vm.diagramId, toEntityRef(c, 'CHANGE_INITIATIVE') ])
            .then(() => loadAssociations());
    };

    vm.onShowAdd = () => {
        vm.visibility.add = true;
    };

    vm.onHideAdd = () => {
        vm.visibility.add = false;
    };


    vm.onAdd = (c) => {
        serviceBroker
            .execute(
                CORE_API.FlowDiagramEntityStore.addRelationship,
                [ vm.diagramId, c ])
            .then(() => {
                loadAssociations();
                vm.onHideAdd();
            });
    };
}


controller.$inject = ['ServiceBroker'];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzFlowDiagramChangeInitiativeAssociations'
};