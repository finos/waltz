const baseState = {
};


const viewState = {
    url: 'actor/{id:int}',
    views: { 'content@': require('./actor-view') }
};


function setup($stateProvider) {
    $stateProvider
        .state('main.actor', baseState)
        .state('main.actor.view', viewState);
}


setup.$inject = ['$stateProvider'];


export default setup;