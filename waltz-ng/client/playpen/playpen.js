const initData = {
    applications: [],
    visibility: {}
};


function controller($q,
                    $stateParams,
                    entityStatisticStore) {

    const vm = Object.assign(this, initData);

    const appIdSelector = {
        entityReference : {
            id: $stateParams.id,
            kind: $stateParams.kind
        },
        scope: $stateParams.kind == 'APP_GROUP' // app-groups always need EXACT as scope
                                    ? "EXACT"
                                    : "CHILDREN"
    };


    // -- LOAD
    entityStatisticStore.findSummaryStatsByIdSelector(appIdSelector)
        .then(stats => {
            vm.entityStatistics = stats;
        });

    global.vm = vm;
}


controller.$inject = [
    '$q',
    '$stateParams',
    'EntityStatisticStore'
];


const view = {
    template: require('./playpen.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;