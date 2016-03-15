import { selectBest, selectWorst, mkSelectByMeasure } from './coloring-strategies';

const BINDINGS = {
    selectedStrategy: '='
};


function controller(perspectiveStore) {

    const vm = this;

    vm.strategies = [
        selectBest,
        selectWorst
    ];

    perspectiveStore.findByCode('BUSINESS')
        .then(p => p.measurables)
        .then(measurables => _.map(measurables, m => mkSelectByMeasure(m)))
        .then(strategies => _.each(strategies, s => vm.strategies.push(s)));

}

controller.$inject = ['PerspectiveStore'];


export default () => ({
    replace: true,
    restrict: 'E',
    controller,
    controllerAs: 'ctrl',
    scope: {},
    bindToController: BINDINGS,
    template: '<span>\n    <select ng-model="ctrl.selectedStrategy" ng-options="option.name for option in ctrl.strategies"></select>\n    <span class="small" ng-bind="ctrl.selectedStrategy.description"></span>\n</span>'
});

