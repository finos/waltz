/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";


const initialState = {
    entries: [],
    entityRef: null
};


function controller($stateParams,
                    changeLogStore) {

    const vm = _.defaultsDeep(this);

    const entityRef = {
        kind: $stateParams.kind,
        id: $stateParams.id,
        name: $stateParams.name
    };

    vm.changeLogTableInitialised = (api) => {
        vm.exportChangeLog = api.export;
    };


    vm.entityRef = entityRef;
    changeLogStore
        .findByEntityReference(entityRef.kind, entityRef.id)
        .then(rs => vm.entries = rs);
}


controller.$inject = [
    '$stateParams',
    'ChangeLogStore'
];


const view = {
    template: require('./view.html'),
    controller,
    controllerAs: 'ctrl'
};


export default view;
