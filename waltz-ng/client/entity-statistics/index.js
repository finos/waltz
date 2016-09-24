
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

    module
        .config(require('./routes'));

    module
        .service('EntityStatisticStore', require('./services/entity-statistic-store'))
        .service('EntityStatisticUtilities', require('./services/entity-statistics-utilities'));

    module
        .directive('waltzEntityStatisticSection', require('./directives/entity-statistic-section'))
        .directive('waltzEntityStatisticBooleanRenderer', require('./directives/entity-statistic-boolean-renderer'))
        .directive('waltzEntityStatisticPercentageRenderer', require('./directives/entity-statistic-percentage-renderer'))
        .directive('waltzEntityStatisticNumericRenderer', require('./directives/entity-statistic-numeric-renderer'));

    module
        .component('waltzEntityStatisticDetailTable', require('./components/entity-statistic-detail-table'))
        .component('waltzEntityStatisticDetailPanel', require('./components/entity-statistic-detail-panel'))
        .component('waltzEntityStatisticSummaryCard', require('./components/entity-statistic-summary-card'))
        .component('waltzEntityStatisticSummarySection', require('./components/entity-statistic-summary-section'))
        .component('waltzEntityStatisticHistoryChart', require('./components/history-chart/entity-statistic-history-chart'))
        .component('waltzEntityStatisticHistoryPanel', require('./components/history-panel/entity-statistic-history-panel'))
        .component('waltzRelatedEntityStatisticsSummaries', require('./components/related-entity-statistics-summaries'));
};
