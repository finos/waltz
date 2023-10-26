/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import angular from "angular";
import {registerComponents, registerStores} from "../common/module-utils";
import DataTypeStore from "./services/data-type-store";
import DataTypeDecoratorStore from "./services/data-type-decorator-store";
import DataTypeUsageCountTree from './components/usage-count-tree/data-type-usage-count-tree';
import DataTypeUsagePanel from './components/usage-panel/data-type-usage-panel';
import DataTypeUsageSelector from './components/usage-selector/data-type-usage-selector';
import DataTypeUsageTree from './components/usage-tree/data-type-usage-tree';
import DataTypeOverview from './components/overview/data-type-overview';
import DataTypeFlowSection from './components/flow-section/data-type-flow-section';
import DataTypeOriginators from './components/originators/data-type-originators';
import RelatedDataTypesSection from './components/related-data-types-section/related-data-types-section';
import DataTypesDecoratorSection from './components/data-type-decorator-section/data-type-decorator-section';
import Routes from './routes';
import DataTypeViewData from './services/data-type-view-data';

export default () => {
    const module = angular.module('waltz.data.types', []);

    module.config(Routes);

    registerStores(module, [ DataTypeStore, DataTypeDecoratorStore ]);
    registerComponents(module, [
        DataTypeOriginators,
        DataTypeFlowSection,
        DataTypeOverview,
        DataTypeUsageCountTree,
        DataTypeUsagePanel,
        DataTypeUsageSelector,
        DataTypeUsageTree,
        RelatedDataTypesSection,
        DataTypesDecoratorSection
    ]);

    module
        .service('DataTypeViewDataService', DataTypeViewData);


    return module.name;
};
