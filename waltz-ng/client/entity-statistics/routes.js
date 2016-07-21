const baseState = {
    url: 'entity-statistic'
};


const viewState = {
    url: '/{kind:string}/{id:int}/{statId:int}',
    views: { 'content@': require('./entity-statistic-view') }
};


function setupRoutes($stateProvider) {
    $stateProvider
        .state('main.entity-statistic', baseState)
        .state('main.entity-statistic.view', viewState)
}

setupRoutes.$inject = ['$stateProvider'];


export default setupRoutes;