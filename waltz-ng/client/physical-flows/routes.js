const baseState = {
};


const viewState = {
    url: 'physical-flow/{id:int}',
    views: {'content@': require('./physical-flow-view') },
};


function setup($stateProvider) {

    $stateProvider
        .state('main.physical-flow', baseState)
        .state('main.physical-flow.view', viewState);
}


setup.$inject = ['$stateProvider'];


export default setup;