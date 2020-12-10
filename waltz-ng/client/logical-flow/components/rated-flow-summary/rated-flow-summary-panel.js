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
import {isEmpty} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";

import template from "./rated-flow-summary-panel.html";
import {entity} from "../../../common/services/enums/entity";


const bindings = {
    filters: "<",
    entityReference: "<"
};


const initialState = {
    infoCell : null,
    visibility: {
        chart: false
    }
};


function findMatchingDecorators(selection, decorators = []) {
    const matcher = {
        decoratorEntity: {
            kind: "DATA_TYPE",
            id: selection.dataType.id
        },
        rating: selection.rating
    };

    return _.filter(decorators, matcher);
}


function findMatchingFlows(flows = [], decorators = []) {
    const flowIds = _.chain(decorators)
        .map("dataFlowId")
        .uniq()
        .value();

    return _.filter(
        flows,
        f => _.includes(flowIds, f.id));
}


function findConsumingApps(flows = [], apps = []) {
    const appsById = _.keyBy(apps, "id");
    return _.chain(flows)
        .filter(f => f.target.kind === "APPLICATION")
        .map("target.id")
        .uniq()
        .map(id => appsById[id])
        .value();
}


/**
 * Only interested in flows coming into this group
 * @param flows
 * @param apps
 * @returns {Array}
 */
function findPotentialFlows(flows = [], apps =[]) {
    const appIds = _.map(apps, "id");
    return _.filter(flows, f => _.includes(appIds, f.target.id));
}


function mkInfoCell(selection, flowData, apps = []) {
    if (! selection) return null;
    if (isEmpty(flowData.flows)) return null;

    const potentialFlows = findPotentialFlows(flowData.flows, apps);

    const matchingDecorators = findMatchingDecorators(selection, flowData.decorators);
    const matchingFlows = findMatchingFlows(potentialFlows, matchingDecorators )
    const consumingApps = findConsumingApps(matchingFlows, apps);

    return { dataType: selection.dataType, rating: selection.rating, applications: consumingApps };
}


function controller(serviceBroker)
{
    const vm = _.defaultsDeep(this, initialState);

    const processSummaries = (xs) => {
        vm.visibility.chart = vm.visibility.chart || _.size(xs) > 0;

        if (vm.entityReference.kind === "DATA_TYPE") {
            const relevantIds = [];
            const descend = (ptr) => {
                relevantIds.push(ptr);
                _.chain(vm.dataTypes)
                    .filter(c => c.parentId === ptr)
                    .forEach(c => descend(c.id))
                    .value();
            };
            descend(vm.entityReference.id);

            return _.filter(xs, x => {
                const decorator = x.decoratorEntityReference;
                return decorator.kind = "DATA_TYPE" && _.includes(relevantIds, decorator.id);
            });
        } else {
            return xs;
        }
    };


    const loadAll = () => {
        const selector = mkSelectionOptions(
            vm.entityReference,
            undefined,
            undefined,
            vm.filters);

        serviceBroker
            .loadViewData(
                CORE_API.ApplicationStore.findBySelector,
                [ selector ])
            .then(r => vm.apps= r.data);

        serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowDecoratorStore.summarizeInboundBySelector,
                [ selector ])
            .then(r => vm.childSummaries = processSummaries(r.data));

        serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowDecoratorStore.summarizeInboundBySelector,
                [ selector ])
            .then(r => vm.exactSummaries = processSummaries(r.data));
    };

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => vm.dataTypes = r.data);
    };

    vm.$onChanges = (changes) => {

        if (!vm.entityReference) return;

        if (changes.filters) {
            loadAll();
        }
    };

    vm.onTableClick = (clickData) => {
        if (clickData.type === "CELL") {

            const selector = mkSelectionOptions(vm.entityReference);

            const flowData = {};
            serviceBroker
                .loadViewData(
                    CORE_API.LogicalFlowStore.findBySelector,
                    [ selector ])
                .then(r => {
                    flowData.flows = r.data;
                    return serviceBroker
                        .loadViewData(
                            CORE_API.DataTypeDecoratorStore.findBySelector,
                            [ selector, entity.LOGICAL_DATA_FLOW.key ]);
                })
                .then(r => {
                    flowData.decorators = r.data;
                    vm.infoPanel = mkInfoCell(clickData, flowData, vm.apps);
                });
        }
    };

}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    template,
    controller,
    bindings
};


export default {
    component,
    id: "waltzRatedFlowSummaryPanel"
};