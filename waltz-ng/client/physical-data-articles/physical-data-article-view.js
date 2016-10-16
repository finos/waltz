const template = require('./physical-data-article-view.html');


function controller($stateParams,
                    applicationStore,
                    bookmarkStore,
                    lineageReportStore,
                    lineageReportContributorStore,
                    logicalDataFlowStore,
                    orgUnitStore,
                    physicalDataArticleStore,
                    physicalDataFlowStore) {
    const vm = this;

    const articleId = $stateParams.id;
    const ref = {
        kind: 'PHYSICAL_DATA_ARTICLE',
        id: articleId
    };

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
}


controller.$inject = [
    '$stateParams',
    'ApplicationStore',
    'BookmarkStore',
    'LineageReportStore',
    'LineageReportContributorStore',
    'DataFlowDataStore', // LogicalDataFlowStore
    'OrgUnitStore',
    'PhysicalDataArticleStore',
    'PhysicalDataFlowStore'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};