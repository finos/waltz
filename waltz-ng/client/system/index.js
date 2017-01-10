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

import angular from 'angular';

import nagMessageService from "./services/nag-message-service";
import settingsStore from "./services/settings-store";
import settingsService from "./services/settings-service";
import hierarchiesStore from "./services/hierarchies-store";
import hasSetting from "./directives/has-setting";

export default () => {

    const module = angular.module('waltz.system', []);

    module
        .service('NagMessageService', nagMessageService)
        .service('SettingsStore', settingsStore)
        .service('SettingsService', settingsService)
        .service('HierarchiesStore', hierarchiesStore);

    module
        .directive('waltzHasSetting', hasSetting);

    module
        .config(require('./routes'));


    return module.name;
};
