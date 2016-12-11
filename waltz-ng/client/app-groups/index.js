
/*
 *  This file is part of Waltz.
 *
 *  Waltz is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Waltz is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import angular from 'angular';

export default () => {

    const module = angular.module('waltz.app.group', []);

    module
        .config(require('./routes'));

    module
        .service('AppGroupStore', require('./services/app-group-store'));

    module
        .directive('waltzAppGroupList', require('./directives/app-group-list'))
        .directive('waltzAppGroupListSection', require('./directives/app-group-list-section'))
        .directive('waltzAppGroupAppSelectionList', require('./directives/app-group-app-selection-list'))
        .directive('waltzAppGroupSummary', require('./directives/app-group-summary'));

    return module.name;
}