const initialState = {
    assetCostData: null,
    applications: [],
    bookmarks: [],
    capabilities: [],
    process: null,
    appCapabilities: {},
    visibility: {}
};


function controller($scope,
                    $stateParams,
                    appCapabilityStore,
                    applicationStore,
                    assetCostViewService,
                    bookmarkStore,
                    processStore,
                    sourceDataRatingStore) {

    const processId = $stateParams.id;

    const selectorOptions = {
        entityReference : {
            id: processId,
            kind: 'PROCESS'
        },
        scope: 'EXACT'
    };

    const vm = Object.assign(this, initialState);

    processStore
        .getById(processId)
        .then(p => vm.process = p);

    processStore
        .findSupportingCapabilities(processId)
        .then(cs => vm.capabilities = cs);

    appCapabilityStore
        .findApplicationCapabilitiesByAppIdSelector(selectorOptions)
        .then(acs => console.log(acs));

    applicationStore
        .findBySelector(selectorOptions)
        .then(apps => vm.applications = apps);

    bookmarkStore
        .findByParent({ id: processId, kind: 'PROCESS'})
        .then(bookmarks => vm.bookmarks = bookmarks);

    assetCostViewService
        .initialise(processId, 'PROCESS', 'EXACT', 2015)
        .then(d => vm.assetCostData = d);

    vm.onAssetBucketSelect = bucket => {
        $scope.$applyAsync(() => {
            assetCostViewService.selectBucket(bucket);
            assetCostViewService
                .loadDetail()
                .then(data => vm.assetCostData = data);
        })
    };

    sourceDataRatingStore
        .findAll()
        .then(rs => vm.sourceDataRatings = rs);
}


controller.$inject = [
    '$scope',
    '$stateParams',
    'AppCapabilityStore',
    'ApplicationStore',
    'AssetCostViewService',
    'BookmarkStore',
    'ProcessStore',
    'SourceDataRatingStore'
];


const view = {
    template: require('./view.html'),
    controller,
    controllerAs: 'ctrl'
};


export default view;