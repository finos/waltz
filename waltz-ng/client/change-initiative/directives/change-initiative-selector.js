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

const initData = {
    results: []
};


const BINDINGS = {
    model: '='
};


function controller(changeInitiativeStore) {
    const vm = Object.assign(this, initData);

    vm.refresh = (query) => {
        if (!query) return;
        return changeInitiativeStore
            .search(query)
            .then((results) => vm.results = results);
    };

}


controller.$inject = ['ChangeInitiativeStore'];


const directive = {
    restrict: 'E',
    replace: true,
    template: require('./change-initiative-selector.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: BINDINGS,
    scope: {}
};


export default () => directive;