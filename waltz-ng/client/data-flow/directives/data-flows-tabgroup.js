import _ from "lodash";


const BINDINGS = {
    flowData: '<',
    applications: '<',
    onLoadDetail: '<',
    options: '=?',
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
            enter: (selection) => console.log("default graphTweaker.node.entry, selection: ", selection)
        }
    }
};


const initialState = {
    applications: [],
    appIds: [],
    boingyEverShown: false,
    dataTypes: [],
    flowData: null,
    onLoadDetail: () => console.log("No onLoadDetail provided for data-flows-tabgroup"),
    options: defaultOptions,
    optionsVisible: false,
    onTabChange: () => console.log("No onTabChange provided for data-flows-tabgroup"),
};


function calculateEntities(flows = []) {
    return _.chain(flows)
        .flatMap(f => [f.source, f.target])
        .uniqBy("id")
        .value();
}


function buildFilter(filterOptions = defaultFilterOptions,
                     appIds = [],
                     flowDecorators = []) {

    const decoratorsByFlowById = _.groupBy(flowDecorators, 'dataFlowId');

    const typeFilterFn = f => {
        const decoratorsForFlow = decoratorsByFlowById[f.id] || [];

        const typeMatchFn = ({ decoratorEntity }) => {
            return decoratorEntity.id === Number(filterOptions.type)
                && decoratorEntity.kind === 'DATA_TYPE';
        };

        return filterOptions.type === 'ALL'
            ? true
            : _.some(decoratorsForFlow, typeMatchFn);
    };

    const scopeFilterFn =  f => {
        switch (filterOptions.scope) {
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

    return f => typeFilterFn(f) && scopeFilterFn(f);

}

function calculateFlowData(allFlows = [],
                           appIds = [],
                           flowDecorators = [],
                           filterOptions = defaultFilterOptions) {

    const filterFn = buildFilter(filterOptions, appIds, flowDecorators);
    const flows = _.filter(allFlows, filterFn);
    const entities = calculateEntities(flows);

    return {flows, entities};
}


function controller($scope, dataFlowUtilityService) {

    const vm = _.defaultsDeep(this, initialState);

    $scope.$watch(
        'ctrl.flowData.flows',
        () => vm.filterChanged(defaultFilterOptions));

    $scope.$watch(
        'ctrl.flowData.decorators',
        (decorators = []) => {
            vm.dataTypes = _.chain(decorators)
                .filter(dc => dc.decoratorEntity.kind === 'DATA_TYPE')
                .map('decoratorEntity.id')
                .uniq()
                .value();

            vm.filterChanged(defaultFilterOptions);
        });

    $scope.$watch(
        'ctrl.applications',
        (applications = []) => {
            vm.appIds = _.map(applications, 'id');
            vm.graphTweakers = dataFlowUtilityService.buildGraphTweakers(vm.appIds)
        });

    vm.filterChanged = (filterOptions) => {
        if (! vm.flowData) return;

        vm.filteredFlowData = calculateFlowData(
            vm.flowData.flows,
            vm.appIds,
            vm.flowData.decorators,
            filterOptions);
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
        vm.onTabChange(tabName, index);
    };

}


controller.$inject = [
    '$scope',
    'DataFlowUtilityService',
    'DataTypeService'
];


const directive = {
    restrict: 'E',
    replace: true,
    scope: {},
    controller,
    controllerAs: 'ctrl',
    bindToController: BINDINGS,
    template: require('./data-flows-tabgroup.html')
};


export default () => directive;
