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
import { initialiseData } from "../../../common";

import { CORE_API } from "../../../common/services/core-api-utils";
import { findUnknownDataTypeId } from "../../../data-types/data-type-utils";
import { categorizeDirection } from "../../../logical-flow/logical-flow-utils";
import { nest } from "d3-collection";

import template from "./application-flow-summary-pane.html";
import {tallyBy} from "../../../common/tally-utils";
import {color} from "d3-color";
import indexByKeyForType from "../../../enum-value/enum-value-utilities";
import {entity} from "../../../common/services/enums/entity";


const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    authoritativeCols: [
        "PRIMARY",
        "SECONDARY",
        "DISCOURAGED",
        "NO_OPINION"
    ],
    visibility: {
        stats: false
    }
};


function enrichDecorators(parentEntityRef, unknownDataTypeId, logicalFlows = [], decorators = []) {
    const logicalFlowsById = _.keyBy(logicalFlows, "id");
    const isKnownDataType = (decorator) => decorator.decoratorEntity.id !== unknownDataTypeId;

    return _
        .chain(decorators)
        .filter(d => d.decoratorEntity.kind === "DATA_TYPE")
        .map(d => {
            const flow = logicalFlowsById[d.dataFlowId];
            return {
                decorator: d,
                logicalFlow: flow,
                direction: categorizeDirection(flow, parentEntityRef),
                mappingStatus: isKnownDataType(d) ? "KNOWN": "UNKNOWN"
            };
        })
        .value();
}


function calcStats(enrichedDecorators = []) {
    const byDirectionAndMappingStatus = nest()
        .key(d => d.direction)
        .key(d => d.mappingStatus)
        .object(enrichedDecorators);

    const byDirectionAndAuthoritativeness = nest()
        .key(d => d.direction)
        .key(d => d.decorator.rating)
        .object(enrichedDecorators);

    const chartData = nest()
        .key(d => d.direction)
        .key(d => d.mappingStatus)
        .key(d => d.decorator.rating)
        .rollup(xs => xs.length)
        .object(enrichedDecorators);

    return {
        byDirectionAndMappingStatus,
        byDirectionAndAuthoritativeness,
        chartData
    };
}

function getFreshnessSummaryConfig() {
    return {
        colorProvider: (d) => color(d.color),
        valueProvider: (d) => d.count,
        idProvider: (d) => d.key,
        labelProvider: d => d.title,
        size: 40
    };
}

function getFreshnessSummaryData(logicalFlows, physicalFlows, enumValues) {
    const logicalFlowIds = logicalFlows
        .map(lf => lf.id);

    const producerOrConsumerPhysicalFlows = physicalFlows
        .filter(pf => logicalFlowIds.includes(pf.logicalFlowId));

    const summaryData = tallyBy(
        producerOrConsumerPhysicalFlows,
        "freshnessIndicator");

    _.each(summaryData, d => d.color = _.get(enumValues, [d.key, "data", "iconColor"], "none"));
    _.each(summaryData, d => d.title = _.get(enumValues, [d.key, "data", "name"], ""));

    return summaryData;
}

function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const reload = (unknownDataTypeId) => {
        const selector = {
            entityReference: vm.parentEntityRef,
            scope: "EXACT"
        };

        const logicalFlowPromise = serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.findByEntityReference,
                [vm.parentEntityRef])
            .then(r => r.data);

        const decorationPromise = serviceBroker
            .loadViewData(
                CORE_API.DataTypeDecoratorStore.findBySelector,
                [selector, entity.LOGICAL_DATA_FLOW.key ])
            .then(r => r.data);

        $q.all([logicalFlowPromise, decorationPromise])
            .then(([logicalFlows, decorators]) => {
                vm.enrichedDecorators = enrichDecorators(
                    vm.parentEntityRef,
                    Number(unknownDataTypeId),
                    logicalFlows,
                    decorators);

                vm.stats = calcStats(vm.enrichedDecorators);
            });

        const physicalFlowPromise = serviceBroker
            .loadViewData(
                CORE_API.PhysicalFlowStore.findByEntityReference,
                [vm.parentEntityRef])
            .then(r => r.data);

        const enumValuePromise = serviceBroker
            .loadAppData(CORE_API.EnumValueStore.findAll)
            .then(r => vm.summaryConfig =
                indexByKeyForType(r.data, "FreshnessIndicator"));

        $q.all([logicalFlowPromise, physicalFlowPromise, enumValuePromise])
            .then(([logicalFlows, physicalFlows]) => {
                vm.freshnessSummaryData = getFreshnessSummaryData(
                    logicalFlows,
                    physicalFlows,
                    vm.summaryConfig);

                vm.freshnessSummaryConfig = getFreshnessSummaryConfig();
            });
    };

    const loadUnknownDataType = () => {
        return serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => findUnknownDataTypeId(r.data));
    };


    vm.$onInit = () => {
        loadUnknownDataType()
            .then(unknownDataType => reload(unknownDataType.id))
    }
}


controller.$inject = [
    "$q",
    "ServiceBroker",
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzApplicationFlowSummaryPane"
};
