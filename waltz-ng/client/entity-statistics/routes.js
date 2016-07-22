const baseState = {
    url: 'entity-statistic'
};


const personViewState = {
    url: '/PERSON/{id:int}/{statId:int}',
    views: { 'content@': require('./person-entity-statistic-view') }
};

const genericViewState = {
    url: '/{kind:string}/{id:int}/{statId:int}',
    views: { 'content@': require('./entity-statistic-view') }
};


function setupRoutes($stateProvider) {
    $stateProvider
        .state('main.entity-statistic', baseState)
        .state('main.entity-statistic.view-person', personViewState)
        .state('main.entity-statistic.view', genericViewState);
}

setupRoutes.$inject = ['$stateProvider'];


export default setupRoutes;