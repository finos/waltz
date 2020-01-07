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

import angular from "angular";
import {CORE_API} from "../../../common/services/core-api-utils";
import template from './person-home.html';


const initialState = {
    person: null
};


function controller($state,
                    serviceBroker) {

    const vm = Object.assign(this, initialState);

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(
                CORE_API.SvgDiagramStore.findByGroup,
                [ 'ORG_TREE' ])
            .then(r => vm.diagrams = r.data);
    };

    vm.blockProcessor = b => {
        b.block.onclick = () => $state.go('main.person.view', { empId: b.value });
        angular.element(b.block).addClass('clickable');
    };

    vm.goToPerson = (person) => {
        $state.go('main.person.id', { id: person.id });
    };

}


controller.$inject = [
    '$state',
    'ServiceBroker'
];


const view = {
    template,
    controllerAs: 'ctrl',
    controller
};


export default view;
