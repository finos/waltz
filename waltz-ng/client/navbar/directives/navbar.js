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

import _ from "lodash";
import {initialiseData} from "../../common";


const navItems = [
    // { uiSref, icon, displayName, <role>, id }
    { uiSref: 'main.org-unit.list', icon: 'sitemap', displayName: 'Org Units', id: 'navbar-org-units' },
    { uiSref: 'main.capability.list', icon: 'puzzle-piece', displayName: 'Functions', id: 'navbar-capabilities' },
    { uiSref: 'main.process.list', icon: 'code-fork', displayName: 'Processes', id: 'navbar-processes', role: 'BETA_TESTER' },
    { uiSref: 'main.person', icon: 'users', displayName: 'People', id: 'navbar-people' },
    { uiSref: 'main.data-type.list', icon: 'qrcode', displayName: 'Data', id: 'navbar-data-types' }
];


const initialState = {
    logoOverlayText: '',
    logoOverlayColor: '#444',
    navItemsForRole: []
};


function getNavItemsFilteredByRole(userService, user, navItems) {
    return _.filter(navItems, i => i.role ? userService.hasRole(user, i.role) : true );
}


function controller(settingsService, userService) {
    const vm = initialiseData(this, initialState);

    settingsService
        .findOrDefault("ui.logo.overlay.text", "")
        .then(setting => vm.logoOverlayText = setting);

    settingsService
        .findOrDefault("ui.logo.overlay.color", "")
        .then(setting => vm.logoOverlayColor = setting);

    userService
        .whoami()
        .then(user => {
            vm.navItemsForRole = getNavItemsFilteredByRole(userService, user, navItems);
        });

}


controller.$inject = ['SettingsService', 'UserService'];


export default () => {
    return {
        restrict: 'E',
        template: require("./navbar.html"),
        controller,
        scope: {},
        controllerAs: 'ctrl'
    };
};
