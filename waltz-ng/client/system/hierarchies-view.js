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
        'PROCESS',
        'PERSON'
    ]
};


function combineTallies(entryCounts = [], rootCounts = []) {

    const rootTalliesKeyed = _.keyBy(rootCounts, 'id');

    return _.chain(entryCounts)
        .map(t => {
            const associatedRootCount = rootTalliesKeyed[t.id] || { count: 0 };
            return {
                id: t.id,
                hierarchyCount: t.count,
                rootCount: associatedRootCount.count
            };
        })
        .value();
}


function controller($q,
                    $state,
                    hierarchiesStore,
                    notification) {

    const vm = initialiseData(this, initialState);


    const loadTallies = () => {
        const promises = [
            hierarchiesStore.findTallies(),
            hierarchiesStore.findRootTallies()
        ];

        $q.all(promises)
            .then( ([tallies, rootTallies]) =>
                vm.combinedTallies = combineTallies(tallies, rootTallies));

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
    'Notification',
];


export default {
    template: require('./hierarchies-view.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


