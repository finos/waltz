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

import {CORE_API} from "../../../common/services/core-api-utils";
import {initialiseData} from '../../../common/index';

import template from './attestation-run-overview.html';


const bindings = {
    run: '<',
};


const initialState = {
    issuedByRef: null
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
        if(vm.run){
            serviceBroker
                .loadViewData(CORE_API.PersonStore.findByUserId, [vm.run.issuedBy])
                .then(r => {
                    vm.issuedByRef = mkEntityRef(r.data);
                });
        }
    }


    vm.getFriendlyScope = (scope) => {
        switch (scope) {
            case 'EXACT':
                return 'only';
            case 'PARENTS':
                return 'and it\'s parents';
            case 'CHILDREN':
                return 'and it\'s children';
            default:
                throw 'Unrecognised scope: ' + scope;
        }
    }
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
    id: 'waltzAttestationRunOverview'
};
