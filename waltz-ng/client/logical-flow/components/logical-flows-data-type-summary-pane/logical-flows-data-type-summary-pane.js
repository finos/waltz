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
import {amber, green, grey, red} from "../../../common/colors";
import {
    findDeprecatedDataTypeIds,
    findNonConcreteDataTypeIds,
    findUnknownDataTypeId
} from "../../../data-types/data-type-utils";

import template from "./logical-flows-data-type-summary-pane.html";
import {mkApplicationSelectionOptions} from "../../../common/selector-utils";
import {entityLifecycleStatus} from "../../../common/services/enums/entity-lifecycle-status";


const bindings = {
    stats: "<",
    parentEntityRef: "<",
    filters: "<"
};


const initialState = {
    visibility: {
        detail: false
    },
    displayBack: false,
};

function prepareSummary(counts = [], unknownId, direction, deprecatedDataTypeIds, nonConcreteDataTypeIds) {
    return _
        .chain(counts)
        .map(d => ({
            typeId: d.dataTypeId,
            name: d.dataTypeName,
            count: (direction === "totalCount") ? d[direction] : d.logicalFlowMeasures[direction] }))
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

function controller(displayNameService, logicalFlowUtilityService, serviceBroker, $q) {
    const vm = initialiseData(this, initialState);

    vm.prepareTable = (dtParent) => {
        vm.detailTable = _
            .chain(vm.typesWithStats)
            .filter(dt => dt.dataType.parentId === _.get(dtParent, ["id"], null))
            .sortBy(dt => dt.dataType.name)
            .value();
        vm.activeParent = dtParent;
    };


    vm.goUp = () => {
        const grandParent = vm.activeParent
            ? vm.dataTypesById[vm.activeParent.parentId]
            : null;
        vm.prepareTable(grandParent);
    };


    const loadData = () => {
        const dataTypePromise = serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => r.data);

        const statsPromise = serviceBroker
            .loadViewData(CORE_API.LogicalFlowDecoratorStore.findDataTypeStatsForEntity, [vm.selector])
            .then(r => r.data);

        return $q
            .all([dataTypePromise, statsPromise])
            .then(([dataTypes, stats]) => {

                const statsByDtId = _.keyBy(stats, "dataTypeId");
                const dtsByParentId = _.groupBy(dataTypes, "parentId");
                vm.dataTypes = dataTypes;
                vm.dataTypesById = _.keyBy(dataTypes, "id");
                vm.allStats = stats;

                vm.typesWithStats = _
                    .chain(dataTypes)
                    .reject(dt => _.isEmpty(statsByDtId[dt.id]))
                    .map(dt => ({
                        dataType: dt,
                        hasChildren: !_.isEmpty(dtsByParentId[dt.id]),
                        stats: statsByDtId[dt.id]}))
                    .value();

                vm.prepareTable(null);
            });
    };


    vm.$onChanges = () => {

        if (vm.parentEntityRef) {
            vm.selector = mkApplicationSelectionOptions(
                vm.parentEntityRef,
                undefined,
                [entityLifecycleStatus.ACTIVE.key],
                vm.filters);
        }

        loadData()
            .then(() => {

                const unknownDataTypeId = findUnknownDataTypeId(vm.dataTypes);
                const deprecatedDataTypeIds = findDeprecatedDataTypeIds(vm.dataTypes);
                const nonConcreteDataTypeIds = findNonConcreteDataTypeIds(vm.dataTypes);

                if (unknownDataTypeId) {
                    vm.visibility.summaries = true;
                    vm.overviewConfig =  {
                        colorProvider: (d) => {
                            if(d.key === "VALID") {
                                return color(green);
                            } else if (d.key === "DEPRECATED") {
                                return color(amber);
                            } else if (d.key === "NON CONCRETE") {
                                return color(grey);
                            } else {
                                return color(red);
                            }
                        },
                        valueProvider: (d) => d.count,
                        idProvider: (d) => d.key,
                        labelProvider: d => _.capitalize(d.key),
                        size: 80
                    };

                    const summaries = [
                        { title: "Intra", prop: "intra"} ,
                        { title: "Inbound", prop: "inbound"} ,
                        { title: "Outbound", prop: "outbound"} ,
                        { title: "All", prop: "totalCount"}
                    ];

                    vm.summaries =  _.map(summaries, d => {
                        return {
                            summary: prepareSummary(
                                vm.allStats,
                                unknownDataTypeId,
                                d.prop,
                                deprecatedDataTypeIds,
                                nonConcreteDataTypeIds),
                            title: d.title
                        }
                    });

                }
            });
    };
}


controller.$inject = [
    "DisplayNameService",
    "LogicalFlowUtilityService",
    "ServiceBroker",
    "$q"
];


const component = {
    bindings,
    controller,
    template
};


export default {
    component,
    id: "waltzLogicalFlowsDataTypeSummaryPane"
};