
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

import angular from 'angular';
import {registerStore} from '../common/module-utils';

import userAgentInfoStore from './services/user-agent-info-store';
import userService from './services/user-service';
import userStore from './services/user-store';
import userPreferenceStore from './services/user-preference-store';
import userPreferenceService from './services/user-preference-service';

import hasRole from './directives/has-role';
import hasRoleForEntityKind from './directives/has-role-for-entity-kind';
import unlessRole from './directives/unless-role';
import ifAnonymous from './directives/if-anonymous';
import Routes from './routes';


export default () => {
    const module = angular.module('waltz.user', []);

    module
        .config(Routes);

    registerStore(module, userStore);

    module
        .service('UserAgentInfoStore', userAgentInfoStore)
        .service('UserService', userService)
        .service('UserPreferenceStore', userPreferenceStore)
        .service('UserPreferenceService', userPreferenceService);

    module
        .directive('waltzHasRole', hasRole)
        .directive('waltzHasRoleForEntityKind', hasRoleForEntityKind)
        .directive('waltzUnlessRole', unlessRole)
        .directive('waltzIfAnonymous', ifAnonymous);

    return module.name;
};
