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

import angular from 'angular';
import {registerComponents, registerStores} from '../common/module-utils';
import * as AssetCostStore from './services/asset-cost-store';
import AssetCostsSection from './components/section/asset-costs-section';
import AssetCostsGraph from './components/graph/asset-costs-graph';
import AssetCostsBasicInfoTile from './components/basic-info-tile/asset-costs-basic-info-tile';
import AssetCostTable from './components/table/asset-cost-table';


export default () => {

    const module = angular.module('waltz.asset.cost', []);

    registerStores(module, [
        AssetCostStore ]);

    registerComponents(module, [
        AssetCostsBasicInfoTile,
        AssetCostsGraph,
        AssetCostsSection,
        AssetCostTable]);

    return module.name;

}