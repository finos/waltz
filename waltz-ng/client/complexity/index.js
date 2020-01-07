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
import {registerStores, registerComponents} from "../common/module-utils";
import * as ComplexityStore from './services/complexity-store';
import * as ComplexityBarChart from './components/chart/complexity-bar-chart';
import * as ComplexitySection from './components/section/complexity-section';

export default () => {
    const module = angular.module('waltz.complexity', []);

    registerStores(module, [ ComplexityStore ]);

    registerComponents(module, [ComplexityBarChart, ComplexitySection]);

    return module.name;
};
