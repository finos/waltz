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
import * as ComplexityScoreStore from './services/complexity-score-store';
import * as ComplexityStore from './services/complexity-store';
import * as ComplexityKindStore from './services/complexity-kind-store';
import * as ComplexityBarChart from './components/chart/complexity-bar-chart';
import ComplexityBasicInfoTile from './components/basic-info-tile/complexity-basic-info-tile';
import * as AppComplexitySummarySection
    from './components/app-complexity-summary-section/app-complexity-summary-section'
import ComplexityGraph from './components/graph/complexity-graph'

export default () => {
    const module = angular.module('waltz.complexity', []);

    registerStores(module, [
        ComplexityScoreStore,
        ComplexityKindStore,
        ComplexityStore ]);

    registerComponents(module, [
        ComplexityBarChart,
        ComplexityBasicInfoTile,
        AppComplexitySummarySection,
        ComplexityGraph]);

    return module.name;
};
