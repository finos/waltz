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

import {initialiseData} from '../common'


const initialState = {
    entries: [],
    entityRef: null
};


function controller($stateParams,
                    attestationStore) {

    const vm = initialiseData(this, initialState);

    const entityRef = {
        kind: $stateParams.kind,
        id: $stateParams.id,
        name: $stateParams.name
    };

    vm.attestationTableInitialised = (api) => {
        vm.exportAttestations = () => api.exportFn("attestations.csv");
    };


    vm.entityRef = entityRef;
    attestationStore
        .findForEntity(entityRef)
        .then(rs => vm.entries = rs);
}


controller.$inject = [
    '$stateParams',
    'AttestationStore'
];


const view = {
    template: require('./view.html'),
    controller,
    controllerAs: 'ctrl'
};


export default view;
