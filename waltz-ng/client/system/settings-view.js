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

function controller(settingsService) {

    const vm = this;

    vm.settings = [];

    const load = (force = false) => settingsService
        .findAll(force)
        .then(settings => vm.settings = settings);

    vm.forceRefresh = () => {
        load(true);
    };

    load();

}

controller.$inject = [ 'SettingsService' ];


export default {
    template: require('./settings-view.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};