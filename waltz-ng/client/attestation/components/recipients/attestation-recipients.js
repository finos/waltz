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
