const baseState = {
};


const viewState = {
    url: 'physical-specification/{id:int}',
    views: {'content@': require('./physical-specification-view') },
};


function setup($stateProvider) {

    $stateProvider
        .state('main.physical-specification', baseState)
        .state('main.physical-specification.view', viewState);
}


setup.$inject = ['$stateProvider'];


export default setup;