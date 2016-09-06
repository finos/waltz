


const baseState = {
    url: 'app-group'
};


const viewState = {
    url: '/{id:int}',
    views: { 'content@': require('./app-group-view') }
};


const editState = {
    url: '/{id:int}/edit',
    views: { 'content@': require('./app-group-edit') }
};


function setupRoutes($stateProvider) {
    $stateProvider
        .state('main.app-group', baseState)
        .state('main.app-group.view', viewState)
        .state('main.app-group.edit', editState);
}

setupRoutes.$inject = ['$stateProvider'];


export default setupRoutes;