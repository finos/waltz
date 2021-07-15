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
import {initialiseData} from "../../../common/index";
import template from "./auth-sources-summary-panel.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import {determineForegroundColor} from "../../../common/colors";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {reduceToSelectedNodesOnly} from "../../../common/hierarchy-utils";
import {loadAuthSourceRatings} from "../../auth-sources-utils";

const bindings = {
    filters: "<",
    parentEntityRef: "<"
};


const initialState = {
    visibility: {
        chart: false
    }
};


function toStats(data = []) {
    return _.reduce(
        data,
        (acc, d) => {
            acc[d.rating] = (acc[d.rating]||0) + d.count;
            return acc;
        },
        {});
}


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const determineIfChartShouldBeVisible = (inboundStats, outboundStats) => {
        const inCount = _.sum(_.values(inboundStats));
        const outCount = _.sum(_.values(outboundStats));
        return (inCount + outCount) > 0;
    };

    const loadSummaryStats = (parentEntityRef, filters, selectedItems=[]) => {
        if (parentEntityRef) {
            const selectionOptions = mkSelectionOptions(
                parentEntityRef,
                undefined,
                undefined,
                filters);

            const ratingsPromise = loadAuthSourceRatings(serviceBroker);

            ratingsPromise
                .then(xs => vm.rowInfo = _
                    .map(
                        xs,
                        d => ({
                            rating: d,
                            style: {
                                "border-radius": "2px",
                                "border-color": d.iconColor,
                                "background-color": d.iconColor,
                                "color": determineForegroundColor(d.iconColor)
                            }
                        })));

            const inboundPromise = serviceBroker
                .loadViewData(
                    CORE_API.LogicalFlowDecoratorStore.summarizeInboundBySelector,
                    [selectionOptions])
                .then(r => r.data);

            const outboundPromise = serviceBroker
                .loadViewData(
                    CORE_API.LogicalFlowDecoratorStore.summarizeOutboundBySelector,
                    [selectionOptions])
                .then(r => r.data);

            $q.all([inboundPromise, outboundPromise, ratingsPromise])
                .then(([inbound, outbound, ratings]) => {
                    //in case user has chosen to selectively plot only some items then we feed them into display separately
                    console.log({inbound, outbound, ratings})
                    const xs = [inbound, outbound];
                    let filteredDataTypes = xs.map(r => {
                        if (selectedItems && selectedItems.length){
                            const reduceable = [...r].map(e => Object.assign({e}, {id :e.decoratorEntityReference.id}));
                            const reduced = reduceToSelectedNodesOnly(reduceable, selectedItems).map(e => e.id);
                            return r.filter(e => reduced.includes(e.decoratorEntityReference.id))
                        }
                        else {
                            return r;
                        }
                    });
                    const [inboundStats, outboundStats] = filteredDataTypes.map(r => toStats(r));
                    vm.visibility.chart = determineIfChartShouldBeVisible(inboundStats, outboundStats);
                    vm.inboundStats = inboundStats;
                    vm.outboundStats = outboundStats;
                    return xs;
                }).then(xs => {
                    const [inboundDataTypes, outboundDataTypes] = xs;
                    const extractDtIdsFn = (myDataTypes) => myDataTypes.map(e => e.decoratorEntityReference.id);
                    const displayDataTypeIds = extractDtIdsFn(inboundDataTypes).concat(extractDtIdsFn(outboundDataTypes));

                    return serviceBroker
                        .loadAppData(CORE_API.DataTypeStore.findAll)
                        .then(result => result.data)
                        .then(dataTypes => dataTypes.map(e => Object.assign(e, {concrete: displayDataTypeIds.includes(e.id)})))
                        .then(dataTypes => reduceToSelectedNodesOnly(dataTypes, displayDataTypeIds));
                }).then(applicableDataTypes => {
                    vm.dataTypes = applicableDataTypes
                });
        }
    };

    vm.onTreeFilterChange = (selectedItems) => {
        loadSummaryStats(vm.parentEntityRef, vm.filters, selectedItems);
    };

    vm.$onInit = () => {
        loadSummaryStats(vm.parentEntityRef, vm.filters);
    };

    vm.$onChanges = (changes) => {
        if (changes.filters) {
            loadSummaryStats(vm.parentEntityRef, vm.filters);
        }
    };
}


controller.$inject = [
    "$q",
    "ServiceBroker"
];


export const component = {
    bindings,
    template,
    controller
};


export const id = "waltzAuthSourcesSummaryPanel";
