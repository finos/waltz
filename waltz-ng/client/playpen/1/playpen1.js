
const initData = {
    filteredFlowData: {
        selectedTypeId: 0,
        decorators: [],
        flows: []
    }
};


function filterByType(typeId, flows = [], decorators = []) {
    if (typeId == 0) {
        return {
            selectedTypeId: 0,
            decorators,
            flows
        };
    }

    const ds = _.filter(decorators, d => d.decoratorEntity.id === typeId);
    const dataFlowIds = _.map(ds, "dataFlowId");
    const fs = _.filter(flows, f => _.includes(dataFlowIds, f.id));

    return {
        selectedTypeId: typeId,
        decorators: ds,
        flows: fs
    };
}


function controller($q,
                    $scope,
                    dataFlowStore,
                    dataFlowDecoratorStore) {
    const vm = Object.assign(this, initData);

    vm.tweakers = {
        type: {
            onSelect: d => {
                d3.event.stopPropagation();
                $scope.$applyAsync(
                    () => vm.filteredFlowData = filterByType(
                        d.id,
                        vm.logicalFlows,
                        vm.dataFlowDecorators));
            }
        },
        typeBlock: {
            onSelect: () => {
                d3.event.stopPropagation();
                $scope.$applyAsync(
                    () => {
                        if (vm.filteredFlowData.selectedTypeId > 0) {
                            vm.showAll();
                        }
                    });
            }
        }
    };

    const appId = 66779;
    const ref = {
        kind: 'APPLICATION',
        id: appId
    };

    const selector = {
        entityReference: ref,
        scope: 'EXACT'
    };

    const flowPromise = dataFlowStore
        .findByEntityReference(ref)
        .then(xs => vm.logicalFlows = xs);

    const decoratorPromise = dataFlowDecoratorStore
        .findBySelectorAndKind(selector)
        .then(xs => vm.dataFlowDecorators = xs);

    vm.entityRef = ref;

    vm.showAll= () => {
        vm.filteredFlowData = filterByType(
            0,
            vm.logicalFlows,
            vm.dataFlowDecorators);
    };

    $q.all([flowPromise, decoratorPromise]).then(() => vm.showAll());



}


controller.$inject = [
    '$q',
    '$scope',
    'DataFlowDataStore',
    'DataFlowDecoratorStore',
    'PhysicalSpecificationStore',
    'PhysicalFlowStore'
];


const view = {
    template: require('./playpen1.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;