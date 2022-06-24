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
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {entityLifecycleStatus} from "../../../common/services/enums/entity-lifecycle-status";

import template from "./logical-flows-boingy-graph.html";
import {buildHierarchies, findNode, flattenChildren} from "../../../common/hierarchy-utils";
import {entity} from "../../../common/services/enums/entity";
import {filterUtils, maybeAddUntaggedFlowsTag} from "../../logical-flow-utils";
import {loadFlowClassificationColorScale} from "../../../flow-classification-rule/flow-classification-utils";
import FlowClassificationLegend from "../../../flow-classification-rule/components/svelte/FlowClassificationLegend.svelte";
import ImageDownloadLink from "../../../common/svelte/ImageDownloadLink.svelte";

const bindings = {
    filters: "<",
    parentEntityRef: "<"
};


const defaultFilterOptions = {
    typeIds: filterUtils.defaultOptions.typeIds,
    scope: "ALL",
    selectedTags: filterUtils.defaultOptions.selectedTags
};


const defaultOptions = {
    graphTweakers: {
        node : {
            enter: (selection) => console.log("default graphTweaker.node.entry, selection: ", selection),
        },
        link : {
            enter: (selection) => selection.attr("stroke", "red")
        }
    }
};


const initialState = {
    FlowClassificationLegend,
    ImageDownloadLink,
    diagramElem: null,
    applications: [],
    flows: [],
    decorators: [],
    tags: [],
    usedDataTypes: [],
    filterOptions: defaultFilterOptions,
    options: defaultOptions,
    visibility: {
        boingyEverShown: false,
        ignoreLimits: false,
        summaries: false,
        loadingFlows: false,
        loadingStats: false
    }
};


function calculateEntities(flows = []) {
    return _.chain(flows)
        .flatMap(f => [f.source, f.target])
        .uniqBy("id")
        .value();
}


function mkScopeFilterFn(appIds = [], scope = "INTRA") {
    return (f) => {
        switch (scope) {
            case "INTRA":
                return _.includes(appIds, f.target.id) && _.includes(appIds, f.source.id);
            case "ALL":
                return true;
            case "INBOUND":
                return _.includes(appIds, f.target.id);
            case "OUTBOUND":
                return _.includes(appIds, f.source.id);
        }
    };
}


function buildFlowFilter(filterOptions = defaultFilterOptions,
                         allFlows = [],
                         appIds = [],
                         flowDecorators = [],
                         allTags = []) {

    const typeFilterFn = filterUtils.mkTypeFilterFn(flowDecorators);
    const scopeFilterFn = mkScopeFilterFn(appIds, filterOptions.scope);
    const tagFilterFn = filterUtils.mkTagFilterFn(filterOptions.selectedTags, allTags, allFlows);
    return f => typeFilterFn(f) && scopeFilterFn(f) && tagFilterFn(f);
}


function calculateFlowData(allFlows = [],
                           applications = [],
                           allDecorators = [],
                           allTags = [],
                           filterOptions = defaultFilterOptions) {
    // note order is important.  We need to find decorators first
    const decoratorFilterFn = filterUtils.buildDecoratorFilter(filterOptions.typeIds);
    const decorators = _.filter(allDecorators, decoratorFilterFn);

    const appIds = _.map(applications, d => d.id);
    const flowFilterFn = buildFlowFilter(filterOptions, allFlows, appIds, decorators, allTags);
    const flows = _.filter(allFlows, flowFilterFn);

    const entities = calculateEntities(flows);

    return {flows, entities, decorators};
}


function getDataTypeIds(allDataTypes = [], decorators = []) {
    const dataTypesById = _.keyBy(allDataTypes, "id");
    return _.chain(decorators)
        .filter(dc => dc.decoratorEntity.kind === "DATA_TYPE")
        .map("decoratorEntity.id")
        .uniq()
        .map(dtId => dataTypesById[dtId])
        .orderBy("name")
        .value();
}


function prepareGraphTweakers(logicalFlowUtilityService,
                              applications = [],
                              decorators = [],
                              flowClassificationColors = () => "grey")
{
    const appIds = _.map(applications, "id");
    return logicalFlowUtilityService.buildGraphTweakers(appIds, decorators, flowClassificationColors);
}


function controller($element,
                    $scope,
                    $q,
                    serviceBroker,
                    logicalFlowUtilityService) {

    const vm = _.defaultsDeep(this, initialState);

    const loadDetail = () => {
        vm.visibility.loadingFlows = true;

        const flowPromise = serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.findBySelector,
                [ vm.selector ])
            .then(r => vm.flows = r.data);

        const decoratorPromise = serviceBroker
            .loadViewData(
                CORE_API.DataTypeDecoratorStore.findBySelector,
                [ vm.selector, entity.LOGICAL_DATA_FLOW.key ])
            .then(r => {
                vm.decorators = r.data;
                return vm.usedDataTypes = getDataTypeIds(vm.allDataTypes, vm.decorators);
            });

        const appsPromise = serviceBroker
            .loadViewData(
                CORE_API.ApplicationStore.findBySelector,
                [ vm.selector ])
            .then(r => vm.applications = r.data);

        const tagsPromise = serviceBroker
            .loadViewData(
                CORE_API.TagStore.findTagsByEntityKindAndTargetSelector,
                [ entity.LOGICAL_DATA_FLOW.key, vm.selector ])
            .then(r => vm.tags = maybeAddUntaggedFlowsTag(r.data));

        return $q
            .all([flowPromise, decoratorPromise, appsPromise, tagsPromise])
            .then(() => vm.filterChanged())
            .then(() => vm.visibility.loadingFlows = false);
    };

    vm.filterChanged = (filterOptions = vm.filterOptions) => {

        const dataTypes = buildHierarchies(vm.allDataTypes, true);
        const node = findNode(dataTypes, Number(filterOptions.typeIds[0]));
        const children = (node)
            ? _.map(flattenChildren(node),d => d.id)
            : [];

        vm.filterOptions = filterOptions;

        vm.filterOptions.typeIds = _.concat(filterOptions.typeIds, children);
        vm.filterOptions.selectedTags = filterOptions.selectedTags;

        if (! (vm.flows && vm.decorators)) return;

        vm.filteredFlowData = calculateFlowData(
            vm.flows,
            vm.applications,
            vm.decorators,
            vm.tags,
            vm.filterOptions);

        vm.graphTweakers = prepareGraphTweakers(
            logicalFlowUtilityService,
            vm.applications,
            vm.filteredFlowData.decorators,
            vm.flowClassificationColors);
    };


    vm.$onChanges = () => {
        if (vm.parentEntityRef) {
            vm.selector = mkSelectionOptions(
                vm.parentEntityRef,
                undefined,
                [entityLifecycleStatus.ACTIVE.key],
                vm.filters);
            loadDetail();
        }
    };

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => vm.allDataTypes = r.data);

        loadFlowClassificationColorScale(serviceBroker).then(r => vm.flowClassificationColors = r);
        vm.diagramElem = document.querySelector(".viz");
    };

}


controller.$inject = [
    "$element",
    "$scope",
    "$q",
    "ServiceBroker",
    "LogicalFlowUtilityService"
];


const component = {
    controller,
    bindings,
    template
};


export default {
    component,
    id: "waltzLogicalFlowsBoingyGraph"
};
