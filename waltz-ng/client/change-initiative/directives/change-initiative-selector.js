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

//todo: make this a component (KS)

import {CORE_API} from '../../common/services/core-api-utils';
import template from './change-initiative-selector.html';


const BINDINGS = {
    model: '='
};


const initData = {
    results: []
};


function controller(serviceBroker) {
    const vm = Object.assign(this, initData);

    vm.refresh = (query) => {
        if (!query) return;
        serviceBroker
            .execute(CORE_API.ChangeInitiativeStore.search, [query])
            .then(result => vm.results = result.data);
    };

}


controller.$inject = [
    'ServiceBroker'
];


const directive = {
    restrict: 'E',
    replace: true,
    template,
    controller,
    controllerAs: 'ctrl',
    bindToController: BINDINGS,
    scope: {}
};


export default () => directive;