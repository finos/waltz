import {initialiseData} from "../common";


const initialState = {
    orphans: [],
};


function controller($q,
                    orphanStore) {

    const vm = initialiseData(this, initialState);


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
                orphanStore.findOrphanChangeInitiatives()
            ])
            .then( ([apps,
                appCaps,
                authSourcesByOrgUnit,
                authSourcesByApp,
                authSourcesByDataType,
                changeInitiatives
            ]) => {
                const orphans = [
                    {description: 'Applications referencing non-existent Org Units', values: apps},
                    {description: 'Application Capabilities mapping to non existent Functions or Apps', values: appCaps},
                    {description: 'Authoritative Sources with non-existent Org Unit', values: authSourcesByOrgUnit},
                    {description: 'Authoritative Sources with non-existent Application', values: authSourcesByApp},
                    {description: 'Authoritative Sources with non-existent Data Type', values: authSourcesByDataType},
                    {description: 'Change Initiatives with non-existent parent', values: changeInitiatives}
                ];
                vm.orphans = orphans;
            });

    };

    loadOrphans();
}


controller.$inject = [
    '$q',
    'OrphanStore'
];


export default {
    template: require('./orphans-view.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


