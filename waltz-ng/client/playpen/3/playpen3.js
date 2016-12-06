import _ from 'lodash';


const initialState = {
    costs: []
};


function controller($scope, assetCostStore, appStore, assetCostViewService) {
    const vm = Object.assign(this, initialState);

    const selector = { entityReference: { id: 10, kind: 'ORG_UNIT'}, scope: 'CHILDREN' };
    assetCostStore
        .findTopAppCostsByAppIdSelector(selector)
        .then(cs => vm.costs = cs);

    assetCostViewService.initialise(selector, 2016)
        .then(costView => vm.costView = costView);

    vm.loadAll = () => {
        assetCostViewService
            .loadDetail()
            .then(costView => vm.costView = costView);
    };

    vm.onHover = (d) => vm.hovered = d;
    vm.onSelect = (d) => vm.selected = d;
}


controller.$inject = [
    '$scope',
    'AssetCostStore',
    'ApplicationStore',
    'AssetCostViewService'
];


const view = {
    template: require('./playpen3.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;