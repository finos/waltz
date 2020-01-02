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
import template from './app-authority-panel.html';
import {initialiseData} from "../../../common/index";
import {nest} from "d3-collection";
import {CORE_API} from "../../../common/services/core-api-utils";


const bindings = {
    authSources: '<'
};


const initialState = {
    nestedAuthSources: null
};


function controller(serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.OrgUnitStore.findAll)
            .then(r => vm.orgUnitsById = _
                .chain(r.data)
                .map(ou => Object.assign({}, ou, { kind: 'ORG_UNIT' }))
                .keyBy('id')
                .value());
    };

    vm.$onChanges = () => {
        vm.nestedAuthSources = nest()
            .key(a => a.dataType)
            .key(a => a.rating)
            .object(vm.authSources || []);
    };

}


controller.$inject = ['ServiceBroker'];


const component = {
    controller,
    bindings,
    template
};


const id = 'waltzAppAuthorityPanel';

export default {
    id,
    component
};






