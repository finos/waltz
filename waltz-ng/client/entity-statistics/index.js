
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
    module.directive('waltzEntityStatisticSection', require('./directives/entity-statistic-section'));
    module.directive('waltzEntityStatisticSummarySection', require('./directives/entity-statistic-summary-section'));
    module.directive('waltzEntityStatisticBooleanRenderer', require('./directives/entity-statistic-boolean-renderer'));
    module.directive('waltzEntityStatisticPercentageRenderer', require('./directives/entity-statistic-percentage-renderer'));
    module.directive('waltzEntityStatisticNumericRenderer', require('./directives/entity-statistic-numeric-renderer'));

    module.service('EntityStatisticStore', require('./services/entity-statistic-store'));

    module.component('waltzEntityStatisticsDetailTable', require('./components/entity-statistics-detail-table'));
};
