const initData = {
    allCapabilities: [],
    appCapabilities: [],
    applications: [],
    ratings: [],
    visibility: {}
};


function controller($q,
                    $stateParams,
                    appStore,
                    appCapabilityStore,
                    capabilityStore,
                    ratingStore) {

    const vm = Object.assign(this, initData);

    const appIdSelector = {
        entityReference : {
            id: $stateParams.id,
            kind: $stateParams.kind
        },
        scope: "CHILDREN"
    };


    // -- LOAD

    appStore
        .findBySelector(appIdSelector)
        .then(apps => vm.applications = apps);

    appCapabilityStore
        .findApplicationCapabilitiesByAppIdSelector(appIdSelector)
        .then(acs => vm.appCapabilities = acs);

    ratingStore
        .findByAppIdSelector(appIdSelector)
        .then(rs => vm.ratings = rs);

    capabilityStore
        .findAll()
        .then(cs => vm.allCapabilities = cs);

}


controller.$inject = [
    '$q',
    '$stateParams',
    'ApplicationStore',
    'AppCapabilityStore',
    'CapabilityStore',
    'RatingStore'
];


const view = {
    template: require('./playpen.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;