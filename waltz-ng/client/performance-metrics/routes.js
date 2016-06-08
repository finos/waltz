import ListView from "./list";
import ItemView from "./view";


const baseState = {
    url: 'performance-metric'
};


const listState = {
    url: '/list',
    views: {'content@': ListView}
};


const viewState = {
    url: '/view/{id:int}',
    views: {'content@': ItemView}
};


function setup($stateProvider) {
    $stateProvider
        .state('main.performance-metric', baseState)
        .state('main.performance-metric.list', listState)
        .state('main.performance-metric.view', viewState);
}


setup.$inject = ['$stateProvider'];


export default setup;