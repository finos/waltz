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

import _ from "lodash";
import {tallyBy} from "../../common/tally-utils";
import {notEmpty} from "../../common";
import {criticalityColorScale, lifecyclePhaseColorScale, variableScale} from "../../common/colors";

import template from "./app-summary.html";

const bindings = {
    apps: "<",
    endUserApps: "<"
};


const initialState = {
    apps: [],
    endUserApps: [],
    visibility: {
        apps: false,
        endUserApps: false
    }
};


const PIE_SIZE = 70;


const defaultLabelProvider = (d) => d.key;
const lifecycleLabelProvider = (displayNameService, d) => displayNameService.lookup("lifecyclePhase", d.key, d.key);
const criticalityLabelProvider = (displayNameService, d) => d ? displayNameService.lookup("criticality", d.key, d.key) : d;
const applicationKindLabelProvider = (displayNameService, d) => displayNameService.lookup("applicationKind", d.key, d.key);

const randomColorProvider = d => variableScale(d.key);
const lifecycleColorProvider = d => lifecyclePhaseColorScale(d.key);
const criticalityColorProvider = d => criticalityColorScale(d.key);


function mkChartData(data,
                     groupingField,
                     size,
                     colorProvider = randomColorProvider,
                     labelProvider = defaultLabelProvider) {
    return {
        config: {
            colorProvider,
            labelProvider,
            size
        },
        data: tallyBy(data, groupingField)
    };
}


function mkCharts(apps = [], endUserApps = [], displayNameService) {
    return {
        apps: {
            byLifecyclePhase: mkChartData(
                apps,
                "lifecyclePhase",
                PIE_SIZE,
                lifecycleColorProvider,
                (k) => lifecycleLabelProvider(displayNameService, k)),
            byKind: mkChartData(
                apps,
                "applicationKind",
                PIE_SIZE,
                randomColorProvider,
                (k) => applicationKindLabelProvider(displayNameService, k))
        },
        endUserApps: {
            byLifecyclePhase: mkChartData(
                endUserApps,
                "lifecyclePhase",
                PIE_SIZE,
                lifecycleColorProvider,
                (k) => lifecycleLabelProvider(displayNameService, k)),
            byKind: mkChartData(
                endUserApps,
                "platform",
                PIE_SIZE),
            byRiskRating: mkChartData(
                endUserApps,
                "riskRating",
                PIE_SIZE,
                criticalityColorProvider,
                (k) => criticalityLabelProvider(displayNameService, k))
        }
    };
}


function calcVisibility(apps = [], endUserApps = []) {
    return {
        apps: notEmpty(apps),
        endUserApps: notEmpty(endUserApps)
    };
}


function controller(displayNameService) {

    const vm = _.defaultsDeep(this, initialState);

    vm.$onChanges = () => {
        vm.charts = mkCharts(vm.apps, vm.endUserApps, displayNameService);
        vm.visibility = calcVisibility(vm.apps, vm.endUserApps);
    };
}


controller.$inject = ["DisplayNameService"];


const component = {
    template,
    controller,
    bindings
};


export default component;
