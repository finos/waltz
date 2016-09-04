import _ from "lodash";
import {initialiseData, kindToViewState} from "../common";

const initialState = {
    combinedTallies: [],
    kinds: [
        'CAPABILITY',
        'CHANGE_INITIATIVE',
        'DATA_TYPE',
        'ENTITY_STATISTIC',
        'ORG_UNIT',
        'PROCESS'
    ]
};

function controller($q,
                    $state,
                    hierarchiesStore,
                    notification) {

    const vm = initialiseData(this, initialState);


    const loadTallies = () => {
        $q
            .all([hierarchiesStore.findTallies(), hierarchiesStore.findRootTallies()])
            .then( ([tallies, rootTallies]) => {
                const rootTalliesKeyed = _.keyBy(rootTallies, 'id');
                vm.combinedTallies = _.chain(tallies)
                    .map(t => ({id: t.id, hierarchyCount: t.count, rootCount: rootTalliesKeyed[t.id].count}) )
                    .value();
            });
    };

    vm.build = (kind) => {
        hierarchiesStore
            .buildForKind(kind)
            .then((count) => notification.success(`Hierarchy rebuilt for ${kind} with ${count} records`))
            .then(loadTallies);
    };


    vm.getRoots = (kind) => {
        hierarchiesStore
            .findRoots(kind)
            .then(roots => vm.roots = roots);
    };


    vm.goToRoot = (entityRef) => {
        if(entityRef.kind === 'ENTITY_STATISTIC') return;
        const stateName = kindToViewState(entityRef.kind);
        $state.go(stateName, { id: entityRef.id});
    };


    loadTallies();
}


controller.$inject = [
    '$q',
    '$state',
    'HierarchiesStore',
    'Notification'
];


export default {
    template: require('./hierarchies-view.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


