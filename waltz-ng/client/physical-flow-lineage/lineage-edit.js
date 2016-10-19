import {initialiseData} from '../common';

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
                    lineageReportStore,
                    logicalDataFlowStore,
                    notification,
                    orgUnitStore,
                    physicalSpecificationStore,
                    physicalFlowStore) {

    const vm = initialiseData(this, initialState);

    const lineageReportId = $stateParams.id;

    const loadReport = () => lineageReportStore
        .getById(lineageReportId)
        .then(lineageReport => vm.lineageReport = lineageReport);

    loadReport()
        .then(lineageReport => physicalSpecificationStore.getById(lineageReport.specificationId))
        .then(specification => vm.specification = specification)
        .then(specification => applicationStore.getById(specification.owningApplicationId))
        .then(app => vm.owningApp = app)
        .then(app => orgUnitStore.getById(app.organisationalUnitId))
        .then(ou => vm.organisationalUnit = ou);

    $scope.$watch('ctrl.selectedApp.app', () =>
        searchViaQuery(vm.selectedApp));

    function resetSearch() {
        vm.searchResults.app = null;
        vm.searchResults.loading = false;
        vm.searchResults.logicalFlows = [];
        vm.searchResults.physicalFlows = [];
        vm.searchResults.specifications = [];
    }

    function searchViaQuery(holder = { app : null }) {
        const app = holder.app;
        if (! app) return;
        return searchForCandidateSpecifications(app.id);
    }

    function searchForCandidateSpecifications( appId ) {
        resetSearch();

        vm.searchResults.loading = true;

        const ref = {
            kind: 'APPLICATION',
            id: appId
        };

        const promises = [
            applicationStore.getById(appId)
                .then(app => vm.searchResults.app = app),
            physicalSpecificationStore
                .findByAppId(appId)
                .then(xs => vm.searchResults.specifications = xs),
            physicalFlowStore
                .findByEntityReference(ref)
                .then(xs => vm.searchResults.physicalFlows = xs),
            logicalDataFlowStore
                .findByEntityReference(ref)
                .then(xs => vm.searchResults.logicalFlows = xs)
        ];

        $q.all(promises)
            .then(() => vm.searchResults.loading = false);
    }


    // -- BOOT
    // TODO: remove!
    applicationStore.getById(67502)
        .then(app => vm.selectedApp = { app });


    // -- INTERACTION

    vm.doSearch = (appRef) => searchForCandidateSpecifications(appRef.id);
    vm.addPhysicalFlowToLineage = (physicalFlowId) => {
        console.log(physicalFlowId);

        // contributorStore.add(articleId, physicalFlowId)
        //     .then(() => reloadContributors(articleId));
    };

    vm.addPhysicalFlowToLineage = (physicalFlowId) => {
        console.log(physicalFlowId);

        // contributorStore.remove(articleId, physicalFlowId)
        //     .then(() => reloadContributors(articleId));
    };

    vm.showReportNameEditor = () => {
        vm.visibility.report.nameEditor = true;
    };

    function update(change) {
        return lineageReportStore.update(Object.assign(change, { id: lineageReportId }))
            .then(() => notification.success('Updated'))
            .then(loadReport);
    }

    vm.updateName = (id, change) => update({ name: change });
    vm.updateDescription = (id, change) => update({ description: change });

}



controller.$inject = [
    '$q',
    '$scope',
    '$stateParams',
    'ApplicationStore',
    'LineageReportStore',
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