

const baseState = {
    url: 'system'
};


const listViewState = {
    url: '/list',
    views: { 'content@': require('./system-admin-list') }
};


const settingsState = {
    url: '/settings',
    views: { 'content@': require('./settings-view') }
};


const hierarchiesState = {
    url: '/hierarchies',
    views: { 'content@': require('./hierarchies-view') }
};


const orphansState = {
    url: '/orphans',
    views: { 'content@': require('./orphans-view') }
};


const recalculateState = {
    url: '/recalculate',
    views: { 'content@': require('./recalculate-view') }
};


function setupRoutes($stateProvider) {
    $stateProvider
        .state('main.system', baseState)
        .state('main.system.list', listViewState)
        .state('main.system.settings', settingsState)
        .state('main.system.hierarchies', hierarchiesState)
        .state('main.system.orphans', orphansState)
        .state('main.system.recalculate', recalculateState);
}

setupRoutes.$inject = ['$stateProvider'];


export default setupRoutes;

