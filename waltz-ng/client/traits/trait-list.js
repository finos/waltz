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


function controller(traitStore) {

    const vm = this;

    vm.loading = false;
    vm.traits = [];

    function load() {
        vm.loading = true;
        traitStore.findAll()
            .then(traits => vm.traits = traits)
            .then(() => vm.loading = false);
    }

    load();

}

controller.$inject = ['TraitStore'];


export default ({
    template: require('./trait-list.html'),
    controllerAs: 'ctrl',
    controller
});