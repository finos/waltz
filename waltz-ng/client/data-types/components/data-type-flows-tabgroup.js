import _ from "lodash";


const bindings = {
    flowData: '<',
    applications: '<',
    options: '<?',
    optionsVisible: '<',
    onTabChange: '<'
};


const template = require('./data-type-flows-tabgroup.html');

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
    dataTypes: [],
    flowData: null,
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


function buildFilter(filterOptions = defaultFilterOptions, appIds) {

    const typeFilterFn = f => filterOptions.type === 'ALL'
        ? true
        : f.dataType === filterOptions.type;

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
                           filterOptions = defaultFilterOptions) {
    const filterFn = buildFilter(filterOptions, appIds);
    const flows = _.filter(allFlows, filterFn);
    const entities = calculateEntities(flows);

    return {flows, entities};
}


function controller($scope, dataFlowUtilityService) {

    const vm = _.defaultsDeep(this, initialState);


    // vm.$onChanges = (changes) => {
    //     if(vm.flowData) {
    //         vm.dataTypes = _.chain(flows)
    //             .map('dataType')
    //             .uniq()
    //             .value();
    //
    //         vm.filterChanged(defaultFilterOptions);
    //     }
    //
    //     if(vm.applications) {
    //         vm.appIds = _.map(applications, 'id');
    //         vm.graphTweakers = dataFlowUtilityService.buildGraphTweakers(vm.appIds);
    //     }
    // }

    $scope.$watch(
        '$ctrl.flowData',
        (flows = []) => {
            vm.dataTypes = _.chain(flows)
                .map('dataType')
                .uniq()
                .value();

            vm.filterChanged(defaultFilterOptions);
        });

    $scope.$watch(
        '$ctrl.applications',
        (applications = []) => {
            vm.appIds = _.map(applications, 'id');
            vm.graphTweakers = dataFlowUtilityService.buildGraphTweakers(vm.appIds)
        });


    vm.filterChanged = (filterOptions) => {
        if (! vm.flowData) return;


        vm.filteredFlowData = calculateFlowData(
            vm.flowData,
            vm.appIds,
            filterOptions);

    };


    vm.tabSelected = (tabName, index) => {
        if(index === 1) {
            vm.boingyEverShown = true;
        }

        vm.onTabChange(tabName, index);
    };

}

controller.$inject = [
    '$scope',
    'DataFlowUtilityService'
];


const component = {
    bindings,
    controller,
    template
};


export default component;