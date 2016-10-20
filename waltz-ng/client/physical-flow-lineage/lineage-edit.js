import {initialiseData} from '../common';
import _ from 'lodash';


const template = require('./lineage-edit.html');


const initialState = {
    selectedApp: { app: null },
    contributors : [],
    searchResults: {
        selectedApp : null,
        logicalFlows: [],
        physicalFlows: [],
        specifications: [],
        loading: false
    },
    visibility: {
        report: {
            nameEditor: false,
            descriptionEditor: false
        },
    }
};


function controller($q,
                    $scope,
                    $stateParams,
                    applicationStore,
                    physicalFlowLineageStore,
                    logicalDataFlowStore,
                    notification,
                    orgUnitStore,
                    physicalSpecificationStore,
                    physicalFlowStore) {

    const vm = initialiseData(this, initialState);

    const physicalFlowId = $stateParams.id;

    const loadLineage = () => physicalFlowLineageStore
        .findByPhysicalFlowId(physicalFlowId)
        .then(lineage => vm.lineage = lineage);

    loadLineage();

    physicalFlowStore.getById(physicalFlowId)
        .then(flow => vm.describedFlow = flow)
        .then(flow => physicalSpecificationStore.getById(flow.specificationId))
        .then(spec => vm.describedSpecification = spec)
        .then(spec => applicationStore.getById(spec.owningEntity.id))
        .then(app => vm.owningApplication = app)
        .then(app => orgUnitStore.getById(app.organisationalUnitId))
        .then(ou => vm.owningOrgUnit = ou)
        .then(app => vm.selectedApp = { app: vm.owningApplication });


    $scope.$watch('ctrl.selectedApp.app', () =>
        searchViaQuery(vm.selectedApp));

    function resetSearch() {
        vm.searchResults.app = null;
        vm.searchResults.loading = false;
        vm.searchResults.logicalFlows = [];
        vm.searchResults.physicalFlows = [];
        vm.searchResults.specifications = [];
        vm.searchResults.relatedApps = [];
    }

    function searchViaQuery(holder = { app : null }) {
        const app = holder.app;
        if (! app) return;
        return searchForCandidateSpecifications(app.id);
    }

    function searchForCandidateSpecifications( appId ) {
        resetSearch();

        vm.searchResults.loading = true;

        const appRef = {
            kind: 'APPLICATION',
            id: appId
        };

        const promises = [
            applicationStore.getById(appId)
                .then(app => vm.searchResults.app = app),
            physicalSpecificationStore
                .findByEntityReference(appRef)
                .then(xs => vm.searchResults.specifications = xs),
            physicalFlowStore
                .findByEntityReference(appRef)
                .then(xs => vm.searchResults.physicalFlows = xs),
            logicalDataFlowStore
                .findByEntityReference(appRef)
                .then(xs => vm.searchResults.logicalFlows = xs)
        ];

        $q.all(promises)
            .then(() => {
                const logicalFlowApps = _.flatMap(vm.searchResults.logicalFlows, lf => [lf.source, lf.target]);

                vm.searchResults.relatedApps = _.chain(logicalFlowApps)
                    .uniqBy("id")
                    .value();
            })
            .then(() => vm.searchResults.loading = false);
    }



    // -- INTERACTION

    vm.doSearch = (appRef) => searchForCandidateSpecifications(appRef.id);

    vm.addPhysicalFlow = (physicalFlowId) => {
        physicalFlowLineageStore
            .addContribution(vm.describedFlow.id, physicalFlowId)
            .then(() => notification.success("added"))
            .then(() => loadLineage());
    };

    vm.removePhysicalFlow = (physicalFlow) => {
        physicalFlowLineageStore
            .removeContribution(vm.describedFlow.id, physicalFlow.id)
            .then(() => notification.warning("removed"))
            .then(() => loadLineage());
    };

}



controller.$inject = [
    '$q',
    '$scope',
    '$stateParams',
    'ApplicationStore',
    'PhysicalFlowLineageStore',
    'DataFlowDataStore', // LogicalDataFlowStore
    'Notification',
    'OrgUnitStore',
    'PhysicalSpecificationStore',
    'PhysicalFlowStore'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};