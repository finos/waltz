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

import template from './welcome.html';


const initialState = {
    appGroupSubscriptions: [],
    panels: [],
    history: []
};


function controller($document,
                    appGroupStore,
                    localStorageService) {

    const vm = Object.assign(this, initialState);

    appGroupStore
        .findMyGroupSubscriptions()
        .then(groupSubscriptions => vm.appGroupSubscriptions = groupSubscriptions);

    vm.history = localStorageService
            .get('history_2') || [];

    $document[0].title = `Waltz`;

}

controller.$inject = [
    '$document',
    'AppGroupStore',
    'localStorageService'
];


const view = {
    controller,
    controllerAs: 'ctrl',
    template
};


export default view;