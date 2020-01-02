
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
