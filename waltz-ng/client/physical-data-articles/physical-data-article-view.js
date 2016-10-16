import {initialiseData} from '../common';


const template = require('./physical-data-article-view.html');


const initialState = {
    visibility: {
        createReportOverlay: false,
        createReportButton: true,
        createReportBusy: false
    },
    createReportForm: {
        name: ""
    }
};



function controller($state,
                    $stateParams,
                    applicationStore,
                    bookmarkStore,
                    lineageReportStore,
                    logicalDataFlowStore,
                    notification,
                    orgUnitStore,
                    physicalDataArticleStore,
                    physicalDataFlowStore)
{
    const vm = initialiseData(this, initialState);

    const articleId = $stateParams.id;
    const ref = {
        kind: 'PHYSICAL_DATA_ARTICLE',
        id: articleId
    };

    // -- LOAD ---

    physicalDataArticleStore
        .getById(articleId)
        .then(article => vm.article = article)
        .then(article => applicationStore.getById(article.owningApplicationId))
        .then(app => vm.owningApp = app)
        .then(app => orgUnitStore.getById(app.organisationalUnitId))
        .then(ou => vm.organisationalUnit = ou);

    physicalDataFlowStore
        .findByArticleId(articleId)
        .then(physicalFlows => vm.physicalFlows = physicalFlows);

    logicalDataFlowStore
        .findByArticleId(articleId)
        .then(logicalFlows => vm.logicalFlows = logicalFlows);

    lineageReportStore
        .findByPhysicalArticleId(articleId)
        .then(lineageReports => vm.lineageReports = lineageReports);

    lineageReportStore
        .findReportsContributedToByArticleId(articleId)
        .then(mentions => vm.lineageMentions = mentions);

    bookmarkStore
        .findByParent(ref)
        .then(bs => vm.bookmarks = bs);


    // -- INTERACT ---

    vm.openCreateReportPopup = () => {
        vm.visibility.createReportOverlay = ! vm.visibility.createReportOverlay;
        if (!vm.createReportForm.name) {
            vm.createReportForm.name = vm.article.name + " Lineage Report";
        }
    };

    vm.canCreateReport = () => {
        const name = vm.createReportForm.name || "";
        return name.length > 1;
    };

    vm.createReport = () => {
        vm.visibility.createReportButton = false;
        vm.visibility.createReportBusy = true;

        lineageReportStore
            .create({ name: vm.createReportForm.name, articleId: vm.article.id })
            .then(reportId => {
                notification.success("Lineage Report created");
                $state.go('main.lineage-report.edit', { id: reportId });
                vm.visibility.createReportButton = true;
                vm.visibility.createReportBusy = false;
            });
    };

}


controller.$inject = [
    '$state',
    '$stateParams',
    'ApplicationStore',
    'BookmarkStore',
    'LineageReportStore',
    'DataFlowDataStore', // LogicalDataFlowStore
    'Notification',
    'OrgUnitStore',
    'PhysicalDataArticleStore',
    'PhysicalDataFlowStore'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};