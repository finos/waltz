const initData = {
    gridData: [],
    filteredGriData: []
};


function controller($interval, lineageStore) {

    const vm = Object.assign(this, initData);

    // vm.owningEntity = {
    //     assetCode:"wltz-01811",
    //     businessCriticality:"UNKNOWN",
    //     description:"All about Bird - 1811",
    //     id:1812,
    //     kind:"EUC",
    //     lifecyclePhase:"CONCEPTUAL",
    //     name:"Bird - 1811",
    //     organisationalUnitId:170,
    //     overallRating:"G",
    //     parentAssetCode:"",
    //     provenance:"waltz"
    // };

    vm.owningEntity = {
        id: 6,
        name: 'Developer',
        kind: 'ACTOR'
    };

    vm.currentEntity = {
        assetCode:"wltz-01866",
        businessCriticality:"UNKNOWN",
        description:"Tiger 5243",
        id:1489,
        kind:"APPLICATION",
        lifecyclePhase:"CONCEPTUAL",
        name:"Bird - 1811",
        organisationalUnitId:170,
        overallRating:"G",
        parentAssetCode:"",
        provenance:"waltz"
    };

    vm.changeTarget = (target) => {
        console.log('change target: ', target);
    };

    vm.dismiss = () => console.log('dismiss');
}


controller.$inject = [
    '$interval',
    'PhysicalFlowLineageStore'
];


const view = {
    template: require('./playpen2.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;