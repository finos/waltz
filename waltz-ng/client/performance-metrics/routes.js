import ListView from "./list";


const baseState = {
    url: 'performance-metric'
};

const listState = {
    url: '/list',
    views: {'content@': ListView}
};



function setup($stateProvider) {
    $stateProvider
        .state('main.performance-metric', baseState)
        .state('main.performance-metric.list', listState);
}

setup.$inject = ['$stateProvider'];


export default setup;