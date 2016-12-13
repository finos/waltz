


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

const baseState = {
    url: 'app-group'
};


const viewState = {
    url: '/{id:int}',
    views: { 'content@': require('./app-group-view') }
};


const editState = {
    url: '/{id:int}/edit',
    views: { 'content@': require('./app-group-edit') }
};


function setupRoutes($stateProvider) {
    $stateProvider
        .state('main.app-group', baseState)
        .state('main.app-group.view', viewState)
        .state('main.app-group.edit', editState);
}

setupRoutes.$inject = ['$stateProvider'];


export default setupRoutes;