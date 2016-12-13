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

    const module = angular.module('waltz.asset.cost', []);

    module
        .service('AssetCostStore', require('./services/asset-cost-store'))
        .service('AssetCostViewService', require('./services/asset-cost-view-service'));

    module
        .component('waltzAssetCostsGraph', require('./components/asset-costs-graph'))
        .component('waltzAssetCostsSection', require('./components/asset-costs-section'));

    module
        .directive('waltzAssetCostTable', require('./directives/asset-cost-table'));

    return module.name;

}