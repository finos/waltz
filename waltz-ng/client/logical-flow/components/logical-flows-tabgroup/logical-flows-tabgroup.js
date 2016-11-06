import _ from "lodash";


const bindings = {
    flowData: '<',
    applications: '<',
    onLoadDetail: '<',
    options: '<',  // { graphTweakers ... }
    optionsVisible: '<',
    onTabChange: '<'
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


function buildFlowFilter(filterOptions = defaultFilterOptions,
                         appIds = [],
                         flowDecorators = []) {
    const typeFilterFn = mkTypeFilterFn(flowDecorators);
    const scopeFilterFn = mkScopeFilterFn(appIds, filterOptions.scope);
    return f => typeFilterFn(f) && scopeFilterFn(f);
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
                           filterOptions = defaultFilterOptions) {
    // note order is important.  We need to find decorators first
    const decoratorFilterFn = buildDecoratorFilter(filterOptions);
    const decorators = _.filter(allDecorators, decoratorFilterFn);

    const appIds = _.map(applications, "id");
    const flowFilterFn = buildFlowFilter(filterOptions, appIds, decorators);
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
            filterOptions);

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
}


controller.$inject = [
    '$scope',
    'LogicalFlowUtilityService',
    'WaltzDisplayNameService'
];


const component = {
    controller,
    bindings,
    template: require('./logical-flows-tabgroup.html')
};


export default component;
