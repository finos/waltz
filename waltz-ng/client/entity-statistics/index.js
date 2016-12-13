
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

    const module = angular.module('waltz.entity.statistics', []);

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
        .component('waltzDurationSelector', require('./components/duration-selector'))
        .component('waltzEntityStatisticDetailTable', require('./components/entity-statistic-detail-table'))
        .component('waltzEntityStatisticDetailPanel', require('./components/entity-statistic-detail-panel'))
        .component('waltzEntityStatisticSummaryCard', require('./components/entity-statistic-summary-card'))
        .component('waltzEntityStatisticSummarySection', require('./components/entity-statistic-summary-section'))
        .component('waltzEntityStatisticHistoryChart', require('./components/history-chart/entity-statistic-history-chart'))
        .component('waltzEntityStatisticHistoryPanel', require('./components/history-panel/entity-statistic-history-panel'))
        .component('waltzEntityStatisticTree', require('./components/entity-statistic-tree'))
        .component('waltzRelatedEntityStatisticsSummaries', require('./components/related-entity-statistics-summaries'));

    return module.name;
};
