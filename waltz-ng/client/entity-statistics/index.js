
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

import angular from "angular";
import {registerComponents, registerStores} from "../common/module-utils";
import entityStatisticStore from "./services/entity-statistic-store";
import entityStatisticSection from "./components/section/entity-statistic-section";
import entityStatisticSummarySection from "./components/entity-statistic-summary-section";
import Routes from './routes';
import EntityStatisticsUtilities from './services/entity-statistics-utilities';
import EntityStatisticBooleanRenderer from './directives/entity-statistic-boolean-renderer';
import EntityStatisticPercentageRenderer from './directives/entity-statistic-percentage-renderer';
import EntityStatisticNumericRenderer from './directives/entity-statistic-numeric-renderer';
import DurationSelector from './components/duration-selector';
import EntityStatisticDetailTable from './components/entity-statistic-detail-table';
import EntityStatisticDetailPanel from './components/entity-statistic-detail-panel';
import EntityStatisticSummaryCard from './components/entity-statistic-summary-card';
import EntityStatisticHistoryChart from './components/history-chart/entity-statistic-history-chart';
import EntityStatisticHistoryPanel from './components/history-panel/entity-statistic-history-panel';
import EntityStatisticTree from './components/entity-statistic-tree';
import EntityStatisticsSummaries from './components/related-entity-statistics-summaries';

export default () => {

    const module = angular.module('waltz.entity.statistics', []);

    module
        .config(Routes);

    registerStores(module, [
        entityStatisticStore
    ]);

    module
        .service('EntityStatisticUtilities', EntityStatisticsUtilities);

    module
        .directive('waltzEntityStatisticBooleanRenderer', EntityStatisticBooleanRenderer)
        .directive('waltzEntityStatisticPercentageRenderer', EntityStatisticPercentageRenderer)
        .directive('waltzEntityStatisticNumericRenderer', EntityStatisticNumericRenderer);

    registerComponents(module, [
        entityStatisticSection,
        entityStatisticSummarySection
    ]);

    module
        .component('waltzDurationSelector', DurationSelector)
        .component('waltzEntityStatisticDetailTable', EntityStatisticDetailTable)
        .component('waltzEntityStatisticDetailPanel', EntityStatisticDetailPanel)
        .component('waltzEntityStatisticSummaryCard', EntityStatisticSummaryCard)
        .component('waltzEntityStatisticHistoryChart', EntityStatisticHistoryChart)
        .component('waltzEntityStatisticHistoryPanel', EntityStatisticHistoryPanel)
        .component('waltzEntityStatisticTree', EntityStatisticTree)
        .component('waltzRelatedEntityStatisticsSummaries', EntityStatisticsSummaries);

    return module.name;
};
