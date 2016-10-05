
const initData = {
};




function controller($scope, dataFlowStore, physicalDataArticleStore, physicalDataFlowStore) {
    const vm = Object.assign(this, initData);


    const appId = 67575;
    const ref = {
        kind: 'APPLICATION',
        id: appId
    };

    physicalDataArticleStore
        .findByAppId(appId)
        .then(xs => vm.articles = xs);

    dataFlowStore
        .findByEntityReference(ref)
        .then(xs => vm.logicalFlows = xs);

    physicalDataFlowStore
        .findByEntityReference(ref)
        .then(xs => vm.physicalFlows = xs);

    global.vm = vm;

}


controller.$inject = [
    '$scope',
    'DataFlowDataStore',
    'PhysicalDataArticleStore',
    'PhysicalDataFlowStore'
];


const view = {
    template: require('./playpen1.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;