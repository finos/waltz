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

function controller(personStore, $q) {
    const vm = this;

    vm.people = [];
    vm.refresh = (query) => {
        if (!query) return $q.resolve([]);
        return personStore
            .search(query)
            .then((people) => {
                vm.people = people;
            });
    };
}

controller.$inject = [
    'PersonStore',
    '$q'
];

export default () => {
    return {
        restrict: 'E',
        replace: true,
        template: require('./person-selector.html'),
        scope: {},
        bindToController: {
            model: '='
        },
        controller,
        controllerAs: 'ctrl'
    };
};

