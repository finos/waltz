import _ from "lodash";
import {initialiseData, kindToViewState} from "../common";

const initialState = {
    combinedTallies: [],
    orphans: [],
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
                    notification,
                    orphanStore) {

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


    vm.showOrphans = (values) => {
        vm.selectedOrphanValues = values;
    };


    const loadOrphans = () => {
        $q
            .all([orphanStore.findAppsWithNonExistentOrgUnits(),
                orphanStore.findOrphanAppCaps(),
                orphanStore.findOrphanAuthoritativeSourcesByOrgUnit(),
                orphanStore.findOrphanAuthoritativeSourcesByApp(),
                orphanStore.findOrphanAuthoritativeSourcesByDataType(),
            ])
            .then( ([apps,
                appCaps,
                authSourcesByOrgUnit,
                authSourcesByApp,
                authSourcesByDataType
            ]) => {
                const orphans = [
                    {description: 'Applications referencing non existent Org Units', values: apps},
                    {description: 'Application Capabilities mapping to non existent Functions or Apps', values: appCaps},
                    {description: 'Authoritative Sources with non-existent Org Unit', values: authSourcesByOrgUnit},
                    {description: 'Authoritative Sources with non-existent Application', values: authSourcesByApp},
                    {description: 'Authoritative Sources with non-existent Data Type', values: authSourcesByDataType}
                ];
                vm.orphans = orphans;
            });

    }

    loadTallies();
    loadOrphans();
}


controller.$inject = [
    '$q',
    '$state',
    'HierarchiesStore',
    'Notification',
    'OrphanStore'
];


export default {
    template: require('./hierarchies-view.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


