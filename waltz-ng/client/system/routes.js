
const baseState = {
    url: 'system'
};


const settingsState = {
    url: '/settings',
    views: { 'content@': require('./settings-view') }
};


const hierarchiesState = {
    url: '/hierarchies',
    views: { 'content@': require('./hierarchies-view') }
};


function setupRoutes($stateProvider) {
    $stateProvider
        .state('main.system', baseState)
        .state('main.system.settings', settingsState)
        .state('main.system.hierarchies', hierarchiesState);
}

setupRoutes.$inject = ['$stateProvider'];


export default setupRoutes;

