
const initData = {
    dataTypes: []
};


function controller(appStore, dataTypeService, dataTypeUsageStore, authSourcesStore, orgUnitStore) {

    const vm = Object.assign(this, initData);

    const entityRef = {
        id: 6000,
        kind: 'DATA_TYPE'
    };

    const selector = {
        entityReference: entityRef,
        scope: 'CHILDREN'
    };

    vm.entityReference = entityRef;

    authSourcesStore
        .findByDataTypeIdSelector(selector)
        .then(authSources => vm.authSources = authSources)
        .then(authSources => _.chain(authSources).map('parentReference.id').uniq().value() )
        .then(orgUnitStore.findByIds)
        .then(orgUnits => vm.orgUnits = orgUnits);

}


controller.$inject = [
    'ApplicationStore',
    'DataTypeService',
    'DataTypeUsageStore',
    'AuthSourcesStore',
    'OrgUnitStore'
];


const view = {
    template: require('./playpen2.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;