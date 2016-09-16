
const initData = {
    apps: [],
    flowData: null
};




function controller(appStore, assetCostStore) {

    const vm = Object.assign(this, initData);

    const entityReference = {
        id: 6591,
        kind: 'APPLICATION'
    };

    const selector = {
        entityReference,
        scope: 'EXACT'
    };


    assetCostStore
        .findAppCostsByAppIdSelector(selector)
        .then(costs => vm.costs = costs);

}


controller.$inject = [
    'ApplicationStore',
    'AssetCostStore'
];


const view = {
    template: require('./playpen1.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;