
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
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