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


function controller(appStore) {

    const vm = this;
    vm.status = 'CLOSED';

    vm.loadRelated = () => {
        vm.status = 'LOADING';
        appStore.findRelatedById(vm.application.id).then(related => {
            vm.related = related;
            vm.status = 'LOADED';
        });
    };
}

controller.$inject = ['ApplicationStore'];

export default () => {
    return {
        restrict: 'E',
        replace: true,
        template: require('./asset-code-explorer.html'),
        scope: {},
        controller,
        controllerAs: 'ctrl',
        bindToController: {
            application: '='
        }
    };
};
