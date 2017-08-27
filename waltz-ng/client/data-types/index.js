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

import angular from "angular";
import {registerComponents, registerStores} from "../common/module-utils";
import DataTypeStore from "./services/data-type-store";
import * as DataTypeTree from './components/data-type-tree';

export default () => {
    const module = angular.module('waltz.data.types', []);

    module.config(require('./routes'));

    registerStores(module, [ DataTypeStore ]);
    registerComponents(module, [ DataTypeTree ]);

    module
        .service('DataTypeViewDataService', require('./services/data-type-view-data'));

    module
        .component('waltzDataTypeOverview', require('./components/data-type-overview'))
        .component('waltzRatedFlowBoingyGraph', require('./components/rated-flow-boingy-graph'))
        .component('waltzDataTypeOriginators', require('./components/data-type-originators'));

    return module.name;
};
