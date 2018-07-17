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

import angular from "angular";
import {registerComponents, registerStores} from "../common/module-utils";
import DataTypeStore from "./services/data-type-store";
import DataTypeUsageCountTree from './components/usage-count-tree/data-type-usage-count-tree';
import DataTypeUsagePanel from './components/usage-panel/data-type-usage-panel';
import DataTypeUsageSelector from './components/usage-selector/data-type-usage-selector';
import DataTypeUsageTree from './components/usage-tree/data-type-usage-tree';
import DataTypeOverview from './components/overview/data-type-overview';
import DataTypeFlowSection from './components/flow-section/data-type-flow-section';
import DataTypeOriginators from './components/originators/data-type-originators';
import RelatedDataTypesSection from './components/related-data-types-section/related-data-types-section';
import Routes from './routes';
import DataTypeViewData from './services/data-type-view-data';

export default () => {
    const module = angular.module('waltz.data.types', []);

    module.config(Routes);

    registerStores(module, [ DataTypeStore ]);
    registerComponents(module, [
        DataTypeOriginators,
        DataTypeFlowSection,
        DataTypeOverview,
        DataTypeUsageCountTree,
        DataTypeUsagePanel,
        DataTypeUsageSelector,
        DataTypeUsageTree,
        RelatedDataTypesSection
    ]);

    module
        .service('DataTypeViewDataService', DataTypeViewData);


    return module.name;
};
