const baseState = {
    url: 'lineage-report',
};


const editState = {
    url: '/{id:int}/edit',
    views: {'content@': require('./lineage-edit') },
};


const viewState = {
    url: '/{id:int}',
    views: {'content@': require('./lineage-view') },
};


function setup($stateProvider) {
    $stateProvider
        .state('main.lineage-report', baseState)
        .state('main.lineage-report.view', viewState)
        .state('main.lineage-report.edit', editState);
}


setup.$inject = ['$stateProvider'];


export default setup;