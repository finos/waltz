const baseState = {
    url: 'change-initiative'
};


const viewState = {
    url: '/{id:int}',
    views: { 'content@': require('./change-initiative-view') }
};


function setupRoutes($stateProvider) {
    $stateProvider
        .state('main.change-initiative', baseState)
        .state('main.change-initiative.view', viewState)
}

setupRoutes.$inject = ['$stateProvider'];


export default setupRoutes;