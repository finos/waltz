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
import {initialiseData} from "../common";

const template = require('./system-admin-list.html');

const initialState = {
    showUserAdminItems: false
};

function controller(userService) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        userService
            .whoami(true) // force
            .then(user => vm.user = user)
            .then(() => vm.showUserAdminItems = userService.hasRole(vm.user, 'ADMIN')
                || userService.hasRole(vm.user, 'USER_ADMIN'));
    };

}

controller.$inject = [ 'UserService' ];


export default {
    template,
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};
