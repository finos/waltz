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

import angular from 'angular';

export default () => {
    const module = angular.module('waltz.data.type.usage', []);

    module
        .directive('waltzAppDataTypeUsageList', require("./directives/app-data-type-usage-list"))

    module
        .component('waltzDataTypeUsageStatTable', require('./components/stat-table/data-type-usage-stat-table'))
        .component('waltzAppDataTypeUsageEditor', require("./components/editor/app-data-type-usage-editor"));

    module
        .service('DataTypeUsageStore', require('./services/data-type-usage-store'));

    return module.name;
};
