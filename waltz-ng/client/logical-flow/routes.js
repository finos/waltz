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

import EditView from "./logical-flow-edit";


function applicationResolver(ApplicationStore, $stateParams) {
    return ApplicationStore.getById($stateParams.id)
}


applicationResolver.$inject = [
    'ApplicationStore',
    '$stateParams'
];

const base = {
    url: 'logical-flow'
};


const editState = {
    url: '/edit/:kind/:id',
    views: {'content@': EditView },
    resolve: {
        application: applicationResolver
    }
};



function setup($stateProvider) {
    $stateProvider
        .state('main.logical-flow', base)
        .state('main.logical-flow.edit', editState);
}


setup.$inject = [
    '$stateProvider'
];


export default setup;