/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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

import _ from "lodash";
import {tallyBy} from "../../common/tally-utils";
import {notEmpty} from "../../common";
import {lifecyclePhaseColorScale, criticalityColorScale, variableScale} from "../../common/colors";

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
