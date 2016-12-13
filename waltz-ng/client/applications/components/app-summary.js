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

import _ from "lodash";
import {tallyBy} from "../../common/tally-utils";
import {notEmpty} from "../../common";
import {lifecyclePhaseColorScale, criticalityColorScale, variableScale} from "../../common/colors";
import {
    criticalityDisplayNames,
    lifecyclePhaseDisplayNames,
    applicationKindDisplayNames
} from "../../common/services/display_names";


const bindings = {
    apps: '<',
    endUserApps: '<'
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
const lifecycleLabelProvider = d => lifecyclePhaseDisplayNames[d.key] || d.key;
const criticalityLabelProvider = d => d ? (criticalityDisplayNames[d.key] || d.key) : d;
const applicationKindLabelProvider = d => applicationKindDisplayNames[d.key] || d.key;

const randomColorProvider = d => variableScale(d.data.key);
const lifecycleColorProvider = d => lifecyclePhaseColorScale(d.data.key);
const criticalityColorProvider = d => criticalityColorScale(d.data.key);


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


function mkCharts(apps = [], endUserApps = []) {
    return {
        apps: {
            byLifecyclePhase: mkChartData(
                apps,
                'lifecyclePhase',
                PIE_SIZE,
                lifecycleColorProvider,
                lifecycleLabelProvider),
            byKind: mkChartData(
                apps,
                'kind',
                PIE_SIZE,
                randomColorProvider,
                applicationKindLabelProvider)
        },
        endUserApps: {
            byLifecyclePhase: mkChartData(
                endUserApps,
                'lifecyclePhase',
                PIE_SIZE,
                lifecycleColorProvider,
                lifecycleLabelProvider),
            byKind: mkChartData(
                endUserApps,
                'platform',
                PIE_SIZE),
            byRiskRating: mkChartData(
                endUserApps,
                'riskRating',
                PIE_SIZE,
                criticalityColorProvider,
                criticalityLabelProvider())
        }
    };
}


function calcVisibility(apps = [], endUserApps = []) {
    return {
        apps: notEmpty(apps),
        endUserApps: notEmpty(endUserApps)
    };
}


function controller() {

    const vm = _.defaultsDeep(this, initialState);

    vm.$onChanges = () => {
        vm.charts = mkCharts(vm.apps, vm.endUserApps);
        vm.visibility = calcVisibility(vm.apps, vm.endUserApps);
    };
}


const component = {
    template: require('./app-summary.html'),
    controller,
    bindings
};


export default component;
