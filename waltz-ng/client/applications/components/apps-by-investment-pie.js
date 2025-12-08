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

import {ragColorScale} from "../../common/colors";
import {toKeyCounts} from "../../common";
import template from "./apps-by-investment-pie.html";


const bindings = {
    applications: "<",
    size: "<"
};




const DEFAULT_SIZE = 80;


const investmentLabels = {
    "R" : "Disinvest",
    "A" : "Maintain",
    "G" : "Invest",
    "N": "Disinvest - Non-Certified",
    "C": "Disinvest - Certified"
};


const config = {
    colorProvider: (d) => ragColorScale(d.key),
    size: DEFAULT_SIZE,
    labelProvider: (d) => investmentLabels[d.key] || "Unknown"
};


function calcAppInvestmentPieStats(apps = []) {
    return toKeyCounts(apps, a => a.overallRating);
}


function controller() {
    const vm = this;

    vm.config = config;
    vm.data = [];

    vm.$onChanges = () => {
        vm.config.size = vm.size
            ? vm.size
            : DEFAULT_SIZE;
        vm.data = calcAppInvestmentPieStats(vm.applications);
    };
}


const component = {
    template,
    bindings,
    controller
};

export default component;
