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

import activeUsersView from "./active-users";
import userManagementView from "./user-management";
import userLogView from "./user-log";

const base = {
    url: 'user'
};


const activeUsersState = {
    url: '/active-users',
    views: {'content@': activeUsersView }
};


const userManagementState = {
    url: '/management',
    views: {'content@': userManagementView }
};


const userLogState = {
    url: '/log',
    views: {'content@': userLogView }
};


function configureStates(stateProvider) {
    stateProvider
        .state('main.user', base)
        .state('main.user.active', activeUsersState)
        .state('main.user.management', userManagementState)
        .state('main.user.log', userLogState);
}


configureStates.$inject = ['$stateProvider'];


export default configureStates;