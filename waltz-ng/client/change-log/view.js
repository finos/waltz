import _ from "lodash";


const initialState = {
    entries: [],
    entityRef: null
};


function controller($stateParams,
                    changeLogStore) {

    const vm = _.defaultsDeep(this);

    const entityRef = {
        kind: $stateParams.kind,
        id: $stateParams.id,
        name: $stateParams.name
    };

    vm.changeLogTableInitialised = (api) => {
        vm.exportChangeLog = api.export;
    };


    vm.entityRef = entityRef;
    changeLogStore
        .findByEntityReference(entityRef.kind, entityRef.id)
        .then(rs => vm.entries = rs);
}


controller.$inject = [
    '$stateParams',
    'ChangeLogStore'
];


const view = {
    template: require('./view.html'),
    controller,
    controllerAs: 'ctrl'
};


export default view;
