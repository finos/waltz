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


const bindings = {
    flowData: '<',
    applications: '<',
    onLoadDetail: '<',
    options: '<',  // { graphTweakers ... }
    optionsVisible: '<',
    onTabChange: '<',
    onTableInitialise: '<'
};


const defaultFilterOptions = {
    type: 'ALL',
    scope: 'INTRA'
};


const defaultOptions = {
    graphTweakers: {
        node : {
            enter: (selection) => console.log("default graphTweaker.node.entry, selection: ", selection),
        },
        link : {
            enter: (selection) => selection.attr('stroke', 'red')
        }
    }
};


const initialState = {
    applications: [],
    selectedApplication: null,
    boingyEverShown: false,
    dataTypes: [],
    enrichedDataTypeCounts: [],
    flowData: null,
    filterOptions: defaultFilterOptions,
    onLoadDetail: () => console.log("No onLoadDetail provided for logical-flows-tabgroup"),
    options: defaultOptions,
    optionsVisible: false,
    onTabChange: () => console.log("No onTabChange provided for logical-flows-tabgroup"),
};


function calculateEntities(flows = []) {
    return _.chain(flows)
        .flatMap(f => [f.source, f.target])
        .uniqBy("id")
        .value();
}


function mkScopeFilterFn(appIds = [], scope = 'INTRA') {
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
        .map('dataFlowId')
        .uniq()
        .value();
    return f => _.includes(flowIds, f.id);
}


function mkIsolatedAppFilterFn(isolatedApp) {
    return isolatedApp
        ? f => f.source.id === isolatedApp.id || f.target.id === isolatedApp.id
        : f => true;
}


function buildFlowFilter(filterOptions = defaultFilterOptions,
                         isolatedApp,
                         appIds = [],
                         flowDecorators = []) {
    const typeFilterFn = mkTypeFilterFn(flowDecorators);
    const scopeFilterFn = mkScopeFilterFn(appIds, filterOptions.scope);
    const isolatedAppFilterFn = mkIsolatedAppFilterFn(isolatedApp);
    return f => typeFilterFn(f) && scopeFilterFn(f) && isolatedAppFilterFn(f);
}


function buildDecoratorFilter(options = defaultFilterOptions) {
    return d => {
        const isDataType = d.decoratorEntity.kind === 'DATA_TYPE';
        const matchesDataType = options.type === 'ALL' || d.decoratorEntity.id === Number(options.type);
        return isDataType && matchesDataType;
    };
}


function calculateFlowData(allFlows = [],
                           applications = [],
                           allDecorators = [],
                           filterOptions = defaultFilterOptions,
                           isolatedApp) {
    // note order is important.  We need to find decorators first
    const decoratorFilterFn = buildDecoratorFilter(filterOptions);
    const decorators = _.filter(allDecorators, decoratorFilterFn);

    const appIds = _.map(applications, "id");
    const flowFilterFn = buildFlowFilter(filterOptions, isolatedApp, appIds, decorators);
    const flows = _.filter(allFlows, flowFilterFn);

    const entities = calculateEntities(flows);

    return {flows, entities, decorators};
}


function getDataTypeIds(decorators = []) {
    return _.chain(decorators)
        .filter(dc => dc.decoratorEntity.kind === 'DATA_TYPE')
        .map('decoratorEntity.id')
        .uniq()
        .value();
}


function prepareGraphTweakers(logicalFlowUtilityService,
                              applications = [],
                              decorators = [],
                              appSelectFn = (d) => console.log("dftg: no appSelectFn given", d))
{
    const appIds = _.map(applications, 'id');
    const tweakers = logicalFlowUtilityService.buildGraphTweakers(appIds, decorators);

    const dfltNodeEnter = tweakers.node.enter;
    const nodeEnter = selection => selection
        .on('click.app-select', appSelectFn)
        .call(dfltNodeEnter);

    tweakers.node.enter = nodeEnter;
    return tweakers;
}


function controller($scope,
                    logicalFlowUtilityService,
                    displayNameService) {

    const vm = _.defaultsDeep(this, initialState);

    function unpinAll() {
        _.forEach(
            vm.filteredFlowData.entities,
            d => { d.fx = null; d.fy = null; });
    }

    vm.$onChanges = () => {
        if (vm.flowData) {
            vm.dataTypes = getDataTypeIds(vm.flowData.decorators);
        }
        if (vm.flowData && vm.flowData.stats) {
            vm.enrichedDataTypeCounts = logicalFlowUtilityService.enrichDataTypeCounts(
                vm.flowData.stats.dataTypeCounts,
                displayNameService);
        }
        vm.filterChanged();
    };

    vm.filterChanged = (filterOptions = vm.filterOptions) => {
        vm.filterOptions = filterOptions;

        if (! vm.flowData) return;

        vm.filteredFlowData = calculateFlowData(
            vm.flowData.flows,
            vm.applications,
            vm.flowData.decorators,
            filterOptions,
            vm.isolatedApplication);

        vm.graphTweakers = prepareGraphTweakers(
            logicalFlowUtilityService,
            vm.applications,
            vm.filteredFlowData.decorators,
            app => $scope.$applyAsync(() => vm.selectedApplication = app));
    };

    vm.loadDetail = () => {
        if (vm.onLoadDetail) {
            vm.onLoadDetail();
        } else {
            console.log("No handler for detail provided ('on-load-detail')");
        }
    };

    vm.tabSelected = (tabName, index) => {
        if(index > 0) {
            vm.loadDetail();
        }
        if(index === 1) {
            vm.boingyEverShown = true;
        }
        vm.currentTabIndex = index;
        vm.onTabChange(tabName, index);
    };

    vm.isolate = (app) => {
        unpinAll();
        vm.isolatedApplication = app;
        vm.filterChanged();
    };

    vm.dismissSelectedApplication = () => {
        unpinAll();
        vm.isolatedApplication = null;
        vm.selectedApplication = null;
        vm.filterChanged();
    };

    vm.refocusApp = app => {
        vm.selectedApplication = app;
        vm.isolate(app);
    }

}


controller.$inject = [
    '$scope',
    'LogicalFlowUtilityService',
    'DisplayNameService'
];


const component = {
    controller,
    bindings,
    template: require('./logical-flows-tabgroup.html')
};


export default component;
