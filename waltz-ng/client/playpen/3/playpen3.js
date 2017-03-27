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

import _ from 'lodash';


const initialState = {
};


function toNode(d, kind = 'APPLICATION') {
    return {
        name: d.name,
        id: d.id,
        description: d.description || '',
        kind
    };
}

function controller($stateParams, applicationStore) {
    const vm = Object.assign(this, initialState);
    applicationStore
        .getById($stateParams.id)
        .then(a => vm.nodes = [toNode(a)]);
}


controller.$inject = [
    '$stateParams',
    'ApplicationStore'
];


const view = {
    template: require('./playpen3.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;
