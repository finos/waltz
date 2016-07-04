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

    module.config(require('./routes'));

    module.directive('waltzAppOverview', require('./directives/app-overview'));
    module.directive('waltzAppOverviewSection', require('./directives/app-overview-section'));
    module.directive('waltzAppSelector', require('./directives/app-selector'));
    module.directive('waltzAssetCodeExplorer', require('./directives/asset-code-explorer'));
    module.directive('waltzAppTable', require('./directives/app-table'));
    module.directive('waltzAppsByInvestmentPie', require('./directives/apps-by-investment-pie'));
    module.directive('waltzAppsByLifecyclePhasePie', require('./directives/apps-by-lifecycle-phase-pie'));
    module.directive('waltzAppSummary', require('./directives/app-summary'));

};
