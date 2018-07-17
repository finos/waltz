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
import * as DataTypeUsageStore from './services/data-type-usage-store';
import {registerStore} from '../common/module-utils'
import AppDataTypeUsageList from "./directives/app-data-type-usage-list";
import DataTypeUsageStatTable from './components/stat-table/data-type-usage-stat-table';
import AppDataTypeUsageEditor from "./components/editor/app-data-type-usage-editor";

export default () => {
    const module = angular.module('waltz.data.type.usage', []);

    module
        .directive('waltzAppDataTypeUsageList', AppDataTypeUsageList)

    module
        .component('waltzDataTypeUsageStatTable', DataTypeUsageStatTable)
        .component('waltzAppDataTypeUsageEditor', AppDataTypeUsageEditor);

    registerStore(module, DataTypeUsageStore);

    return module.name;
};
