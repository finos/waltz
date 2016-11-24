/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */


export default (module) => {

    require('./services')(module);

    module
        .config(require('./routes'));

    module
        .directive('waltzAppOverview', require('./directives/app-overview'))
        .directive('waltzAppOverviewSection', require('./directives/app-overview-section'))
        .directive('waltzAppSelector', require('./directives/app-selector'))
        .directive('waltzAssetCodeExplorer', require('./directives/asset-code-explorer'))
        .directive('waltzBasicAppSelector', require('./directives/basic-app-selector'));

    module
        .component('waltzAppsByInvestmentPie', require('./components/apps-by-investment-pie'))
        .component('waltzAppsByLifecyclePhasePie', require('./components/apps-by-lifecycle-phase-pie'))
        .component('waltzAppsSection', require('./components/apps-section/apps-section'))
        .component('waltzAppSummary', require('./components/app-summary'))
        .component('waltzAppTable', require('./components/app-table'))
        .component('waltzAppCostsSection', require('./components/app-costs-section/app-costs-section'));
};
