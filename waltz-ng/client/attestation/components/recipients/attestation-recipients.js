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
import {CORE_API} from "../../../common/services/core-api-utils";
import {initialiseData} from '../../../common';
import template from './attestation-recipients.html';


const bindings = {
    instance: '<'
};

const initialState = {
    recipientReferences: [],
    visibility: {
        loading: false
    }
};

function mkEntityRef(person) {
    if (person) {
        return {
            id: person.id,
            name: person.displayName,
            kind: 'PERSON'
        };
    }
    return person;
}

function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {

        if(vm.instance){
            vm.recipientReferences = [];
            vm.visibility.loading = true;
            serviceBroker
                .loadViewData(CORE_API.AttestationInstanceStore.findPersonsById, [vm.instance.id])
                .then(r => {
                    vm.recipientReferences = _.map(r.data, mkEntityRef) ;
                    vm.visibility.loading = false;
                });
        }
    };
}


controller.$inject = [
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzAttestationRecipients'
};
