import _ from "lodash";


const initData = {
    list: [],
    model: null,
    flowOptionsVisible: false
};


const DEFAULT_OPTIONS = {
    type: 'ALL',
    scope: 'INTRA'
};


function calculateEntities(flows = []) {
    return _.chain(flows)
        .flatMap(f => [f.source, f.target])
        .uniqBy("id")
        .value();
}

function buildFilter(filterOptions = DEFAULT_OPTIONS, appIds) {

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
                           filterOptions = DEFAULT_OPTIONS) {


    const filterFn = buildFilter(filterOptions, appIds);
    const flows = _.filter(allFlows, filterFn);
    const entities = calculateEntities(flows);

    return {flows, entities};
}

function controller($scope,
                    appStore,
                    dataFlowViewService,
                    dataFlowUtilityService) {
    const vm = Object.assign(this, initData);
    const orgUnitId = 40;
    let originalFlowData = null;


    dataFlowViewService.initialise(orgUnitId, 'ORG_UNIT', 'CHILDREN')
        .then(() => dataFlowViewService.loadDetail())
        .then(flowData => {
            originalFlowData = {...flowData };
            vm.dataTypes = _.chain(flowData.flows)
                .map('dataType')
                .uniq()
                .value();
            vm.flowData = calculateFlowData(originalFlowData.flows, vm.appIds, DEFAULT_OPTIONS);
        });

    appStore.findByOrgUnitTree(orgUnitId)
        .then(apps => vm.appIds = _.map(apps, "id"))
        .then(appIds => vm.graphTweakers = dataFlowUtilityService.buildGraphTweakers(appIds))


    vm.filterChanged = (options) => {
        if (! originalFlowData) return;
        
        vm.flowData = calculateFlowData(
            originalFlowData.flows,
            vm.appIds,
            options);
    }

    global.vm = vm;
}


controller.$inject = [
    '$scope',
    'ApplicationStore',
    'DataFlowViewService',
    'DataFlowUtilityService'
];


const view = {
    template: require('./playpen.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;