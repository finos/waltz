/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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
import {mkSelectionOptions} from "../../../common/selector-utils";
import template from "./logical-flows-boingy-graph.html";

const bindings = {
    parentEntityRef: "<"
};


const defaultFilterOptions = {
    type: "ALL",
    scope: "INTRA"
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
    applications: [],
    flows: [],
    decorators: [],
    selectedNode: null,
    isolatedNode: null,
    usedDataTypes: [],
    filterOptions: defaultFilterOptions,
    options: defaultOptions,
    optionsVisible: false,
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


function mkTypeFilterFn(decorators = []) {
    const flowIds = _.chain(decorators)
        .map("dataFlowId")
        .uniq()
        .value();
    return f => _.includes(flowIds, f.id);
}


function mkIsolatedNodeFilterFn(isolatedNode) {
    return isolatedNode
        ? f => f.source.id === isolatedNode.id || f.target.id === isolatedNode.id
        : () => true;
}


function buildFlowFilter(filterOptions = defaultFilterOptions,
                         isolatedNode,
                         appIds = [],
                         flowDecorators = []) {
    const typeFilterFn = mkTypeFilterFn(flowDecorators);
    const scopeFilterFn = mkScopeFilterFn(appIds, filterOptions.scope);
    const isolatedNodeFilterFn = mkIsolatedNodeFilterFn(isolatedNode);
    return f => typeFilterFn(f) && scopeFilterFn(f) && isolatedNodeFilterFn(f);
}


function buildDecoratorFilter(options = defaultFilterOptions) {
    return d => {
        const isDataType = d.decoratorEntity.kind === "DATA_TYPE";
        const matchesDataType = options.type === "ALL" || d.decoratorEntity.id === Number(options.type);
        return isDataType && matchesDataType;
    };
}


function calculateFlowData(allFlows = [],
                           applications = [],
                           allDecorators = [],
                           filterOptions = defaultFilterOptions,
                           isolatedNode) {
    // note order is important.  We need to find decorators first
    const decoratorFilterFn = buildDecoratorFilter(filterOptions);
    const decorators = _.filter(allDecorators, decoratorFilterFn);

    const appIds = _.map(applications, "id");
    const flowFilterFn = buildFlowFilter(filterOptions, isolatedNode, appIds, decorators);
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
                              nodeSelectFn = (d) => console.log("dftg: no nodeSelectFn given", d))
{
    const appIds = _.map(applications, "id");
    const tweakers = logicalFlowUtilityService.buildGraphTweakers(appIds, decorators);

    const dfltNodeEnter = tweakers.node.enter;
    const nodeEnter = selection => selection
        .on("click.node-select", nodeSelectFn)
        .call(dfltNodeEnter);

    tweakers.node.enter = nodeEnter;
    return tweakers;
}


function controller($scope,
                    $q,
                    serviceBroker,
                    logicalFlowUtilityService) {

    const vm = _.defaultsDeep(this, initialState);

    function unpinAll() {
        _.forEach(
            vm.filteredFlowData.entities,
            d => { d.fx = null; d.fy = null; });
    }

    const loadDetail = () => {
        vm.visibility.loadingFlows = true;

        const flowPromise = serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.findBySelector,
                [ vm.selector ])
            .then(r => vm.flows = r.data);

        const decoratorPromise = serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowDecoratorStore.findBySelector,
                [ vm.selector ])
            .then(r => {
                vm.decorators = r.data;
                vm.usedDataTypes = getDataTypeIds(vm.allDataTypes, vm.decorators);
            });

        const appsPromise = serviceBroker
            .loadViewData(
                CORE_API.ApplicationStore.findBySelector,
                [ vm.selector ])
            .then(r => vm.applications = r.data);

        return $q
            .all([flowPromise, decoratorPromise, appsPromise])
            .then(() => {
                vm.filterChanged();
                vm.visibility.loadingFlows = false;
            });
    };

    vm.filterChanged = (filterOptions = vm.filterOptions) => {
        vm.filterOptions = filterOptions;

        if (! (vm.flows && vm.decorators)) return;

        vm.filteredFlowData = calculateFlowData(
            vm.flows,
            vm.applications,
            vm.decorators,
            filterOptions,
            vm.isolatedNode);

        vm.graphTweakers = prepareGraphTweakers(
            logicalFlowUtilityService,
            vm.applications,
            vm.filteredFlowData.decorators,
            node => $scope.$applyAsync(() => vm.selectedNode = node));
    };

    vm.isolate = (node) => {
        unpinAll();
        vm.isolatedNode = node;
        vm.filterChanged();
    };

    vm.dismissSelectedNode = () => {
        unpinAll();
        vm.isolatedNode = null;
        vm.selectedNode = null;
        vm.filterChanged();
    };

    vm.refocusNode = node => {
        vm.selectedNode = node;
        vm.isolate(node);
    };

    vm.$onChanges = (c) => {
        if (vm.parentEntityRef) {
            vm.selector = mkSelectionOptions(vm.parentEntityRef);
            loadDetail();
        }
    };

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => vm.allDataTypes = r.data);
    };

}


controller.$inject = [
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
