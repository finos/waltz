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
import DataTypeTree from './components/tree/data-type-tree';
import DataTypeOverview from './components/overview/data-type-overview';
import DataTypeFlowSection from './components/flow-section/data-type-flow-section';
import DataTypeOriginators from './components/originators/data-type-originators';

export default () => {
    const module = angular.module('waltz.data.types', []);

    module.config(require('./routes'));

    registerStores(module, [ DataTypeStore ]);
    registerComponents(module, [
        DataTypeOriginators,
        DataTypeFlowSection,
        DataTypeOverview,
        DataTypeTree
    ]);

    module
        .service('DataTypeViewDataService', require('./services/data-type-view-data'));


    return module.name;
};
