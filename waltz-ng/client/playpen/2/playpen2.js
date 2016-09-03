import { loadDataFlowDecorators, loadDataFlows } from '../../applications/data-load';


const initData = {

};


function controller($scope, flowStore, decoratorStore) {

    const vm = Object.assign(this, initData);

    const entityRef = {
        id: 6665,
        kind: 'APPLICATION'
    };

    vm.entityRef = entityRef;

    loadDataFlows(flowStore, entityRef.id, vm)
        .then(() => vm.all());

    loadDataFlowDecorators(decoratorStore, entityRef.id, vm)
        ;

    vm.tweakers = {
        app: {
            onSelect: (d) => { vm.selected = d; $scope.$apply(); }
        },
        type: {
            onSelect: (d) => { vm.selected = d; $scope.$apply(); }
        }
    };

    vm.some = () => {
        vm.dataFlows = _.filter(vm.flows, f => f.source.id % 2 == 0)
    };

    vm.all = () => {
        vm.dataFlows = vm.flows;
    };

}


controller.$inject = [
    '$scope',
    'DataFlowDataStore',
    'DataFlowDecoratorStore'
];


const view = {
    template: require('./playpen2.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;