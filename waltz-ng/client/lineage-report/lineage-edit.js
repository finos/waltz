import {initialiseData} from '../common';

const template = require('./lineage-edit.html');


const initialState = {
    selectedApp: { app: null },
    contributors : [],
    searchResults: {
        selectedApp : null,
        logicalFlows: [],
        physicalFlows: [],
        articles: [],
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
                    orgUnitStore,
                    physicalDataArticleStore,
                    physicalDataFlowStore) {

    const vm = initialiseData(this, initialState);

    const lineageReportId = $stateParams.id;

    lineageReportStore
        .getById(lineageReportId)
        .then(lineageReport => vm.lineageReport = lineageReport)
        .then(lineageReport => physicalDataArticleStore.getById(lineageReport.physicalArticleId))
        .then(article => vm.article = article)
        .then(article => applicationStore.getById(article.owningApplicationId))
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
        vm.searchResults.articles = [];
    }

    function searchViaQuery(holder = { app : null }) {
        const app = holder.app;
        if (! app) return;
        return searchForCandidateArticles(app.id);
    }

    function searchForCandidateArticles( appId ) {
        resetSearch();

        vm.searchResults.loading = true;

        const ref = {
            kind: 'APPLICATION',
            id: appId
        };

        const promises = [
            applicationStore.getById(appId)
                .then(app => vm.searchResults.app = app),
            physicalDataArticleStore
                .findByAppId(appId)
                .then(xs => vm.searchResults.articles = xs),
            physicalDataFlowStore
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

    vm.doSearch = (appRef) => searchForCandidateArticles(appRef.id);
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
    }

}



controller.$inject = [
    '$q',
    '$scope',
    '$stateParams',
    'ApplicationStore',
    'LineageReportStore',
    'DataFlowDataStore',
    'OrgUnitStore',
    'PhysicalDataArticleStore',
    'PhysicalDataFlowStore'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};