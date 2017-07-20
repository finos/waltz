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


export default () => {

    const module = angular.module('waltz.applications', []);

    require('./services')(module);

    module
        .config(require('./routes'));

    module
        .directive('waltzAppOverviewSection', require('./directives/app-overview-section'))
        .directive('waltzAppSelector', require('./directives/app-selector'))
        .directive('waltzAssetCodeExplorer', require('./directives/asset-code-explorer'))
        .directive('waltzBasicAppSelector', require('./directives/basic-app-selector'));

    module
        .component('waltzAppsByInvestmentPie', require('./components/apps-by-investment-pie'))
        .component('waltzAppsByLifecyclePhasePie', require('./components/apps-by-lifecycle-phase-pie'))
        .component('waltzAppOverview', require('./components/app-overview'))
        .component('waltzAppsSection', require('./components/apps-section/apps-section'))
        .component('waltzAppSummary', require('./components/app-summary'))
        .component('waltzAppTable', require('./components/app-table'))
        .component('waltzAppCostsSection', require('./components/app-costs-section/app-costs-section'));

    return module.name;
};
