const baseState = {};


function setup($stateProvider) {
    $stateProvider
        .state('main.measurable-rating.replacement', baseState);
}

setup.$inject = ['$stateProvider'];

export default setup;