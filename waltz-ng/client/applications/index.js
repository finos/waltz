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

import { registerComponents } from '../common/module-utils';

import routes from './routes';
import services from './services';

import AppCostsSection from './components/app-costs-section/app-costs-section';
import AppOverview from './components/overview/app-overview';
import BulkApplicationSelector from "./components/bulk-application-selector/bulk-application-selector";
import RelatedAppsSection from './components/related-apps-section/related-apps-section';
import AppSelector from './directives/app-selector';
import AssetCodeExplorer from './directives/asset-code-explorer';
import BasicAppSelector from './directives/basic-app-selector';
import AppsByInvestmentPie from './components/apps-by-investment-pie';
import AppsByLifecyclePhasePie from './components/apps-by-lifecycle-phase-pie';
import AppsSection from './components/apps-section/apps-section';
import AppSummary from './components/app-summary';
import AppTable from './components/app-table';

export default () => {

    const module = angular.module('waltz.applications', []);

    services(module);

    module
        .config(routes);

    module
        .directive('waltzAppSelector', AppSelector)
        .directive('waltzAssetCodeExplorer', AssetCodeExplorer)
        .directive('waltzBasicAppSelector', BasicAppSelector);

    module
        .component('waltzAppsByInvestmentPie', AppsByInvestmentPie)
        .component('waltzAppsByLifecyclePhasePie', AppsByLifecyclePhasePie)
        .component('waltzAppsSection', AppsSection)
        .component('waltzAppSummary', AppSummary)
        .component('waltzAppTable', AppTable);

    registerComponents(module, [
        AppCostsSection,
        AppOverview,
        BulkApplicationSelector,
        RelatedAppsSection
    ]);

    return module.name;
};
