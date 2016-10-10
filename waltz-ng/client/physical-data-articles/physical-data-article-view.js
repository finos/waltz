const template = require('./physical-data-article-view.html');


function controller($stateParams,
                    applicationStore,
                    logicalDataFlowStore,
                    orgUnitStore,
                    physicalDataArticleStore,
                    physicalDataFlowStore) {
    const vm = this;

    const articleId = $stateParams.id;

    physicalDataArticleStore
        .findById(articleId)
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
}


controller.$inject = [
    '$stateParams',
    'ApplicationStore',
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