
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
import AppGroupSummary from './components/summary/app-group-summary'
import RelatedAppGroupsSection from './components/related-app-groups-section/related-app-groups-section';
import {registerComponents, registerStores} from "../common/module-utils";
import * as AppGroupStore from './services/app-group-store';

export default () => {

    const module = angular.module('waltz.app.group', []);

    module
        .config(require('./routes'));

    module
        .directive('waltzAppGroupList', require('./directives/app-group-list'))
        .directive('waltzAppGroupListSection', require('./directives/app-group-list-section'))
        .directive('waltzAppGroupAppSelectionList', require('./directives/app-group-app-selection-list'));

    registerComponents(module, [
        AppGroupSummary,
        RelatedAppGroupsSection ]);

    registerStores(module, [ AppGroupStore ]);

    return module.name;

}
