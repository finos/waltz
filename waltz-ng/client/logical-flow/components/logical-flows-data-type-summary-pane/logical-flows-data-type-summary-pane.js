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
import {CORE_API} from "../../../common/services/core-api-utils";
import {initialiseData} from "../../../common/index";
import {color} from "d3-color";
import {amber, green, red, grey} from "../../../common/colors";
import {findNonConcreteDataTypeIds, findDeprecatedDataTypeIds, findUnknownDataTypeId} from "../../../data-types/data-type-utils";

import template from "./logical-flows-data-type-summary-pane.html";


const bindings = {
    stats: "<"
};


const initialState = {
    visibility: {
        detail: false
    }
};

function prepareSummary(counts = [], unknownId, direction, deprecatedDataTypeIds, nonConcreteDataTypeIds) {
    return _
        .chain(counts)
        .map(d => ({
            typeId: d.dataType.id,
            name: d.dataType.name,
            count: d[direction] }))
        .reduce((acc, d) => {
            if (d.typeId === Number(unknownId)) {
                acc.UNKNOWN  += d.count;
            } else if (deprecatedDataTypeIds.includes(d.typeId)){
                acc.DEPRECATED += d.count;
            } else if (nonConcreteDataTypeIds.includes(d.typeId)){
                acc.NON_CONCRETE += d.count;
            } else {
                acc.VALID += d.count;
            }
            return acc;
        }, { VALID: 0, UNKNOWN : 0, DEPRECATED : 0, NON_CONCRETE : 0})
        .map((v, k) => ({ key: friendlyName(k), count: v }))
        .value();
}

function friendlyName(name) {
    return name.replace("_", " ");
}

function controller(displayNameService, logicalFlowUtilityService, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadDataTypes = () => {
        return serviceBroker.loadAppData(CORE_API.DataTypeStore.findAll);
    };

    vm.$onChanges = () => {

        if (! vm.stats) return;

        vm.enrichedDataTypeCounts = logicalFlowUtilityService.enrichDataTypeCounts(
            vm.stats.dataTypeCounts,
            displayNameService);

        loadDataTypes()
            .then(dt => {
                const dataTypes = dt.data;
                const unknownDataTypeId = findUnknownDataTypeId(dataTypes);
                const deprecatedDataTypeIds = findDeprecatedDataTypeIds(dataTypes);
                const nonConcreteDataTypeIds = findNonConcreteDataTypeIds(dataTypes);

                if (unknownDataTypeId) {
                    vm.visibility.summaries = true;
                    vm.summaryConfig =  {
                        colorProvider: (d) => {
                            if(d.data.key === "VALID") {
                                return color(green);
                            } else if (d.data.key === "DEPRECATED") {
                                return color(amber);
                            } else if (d.data.key === "NON CONCRETE") {
                                return color(grey);
                            } else {
                                return color(red);
                            }
                        },
                        valueProvider: (d) => d.count,
                        idProvider: (d) => d.data.key,
                        labelProvider: d => _.capitalize(d.key),
                        size: 40
                    };

                    vm.overviewConfig =  Object.assign({}, vm.summaryConfig, { size: 80 });

                    const summaries = [
                        { title: "Intra", prop: "intra"} ,
                        { title: "Inbound", prop: "inbound"} ,
                        { title: "Outbound", prop: "outbound"} ,
                        { title: "All", prop: "total"}
                    ];

                    vm.summaries= _.map(summaries, d => {
                        return {
                            summary: prepareSummary(vm.enrichedDataTypeCounts,
                                unknownDataTypeId,
                                d.prop,
                                deprecatedDataTypeIds,
                                nonConcreteDataTypeIds),
                            title: d.title
                        }
                    });

                }
            });
    }
}


controller.$inject = [
    "DisplayNameService",
    "LogicalFlowUtilityService",
    "ServiceBroker",
];


const component = {
    bindings,
    controller,
    template
};




export default {
    component,
    id: "waltzLogicalFlowsDataTypeSummaryPane"
}