const baseState = {};


function setup($stateProvider) {
    $stateProvider
        .state('main.measurable-rating.planned-decommission', baseState);
}

setup.$inject = ['$stateProvider'];

export default setup;