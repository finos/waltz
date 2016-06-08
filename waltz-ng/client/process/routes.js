import ListView from "./list";
import ItemView from "./view";


const baseState = {
    url: 'process'
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
        .state('main.process', baseState)
        .state('main.process.list', listState)
        .state('main.process.view', viewState);
}

setup.$inject = ['$stateProvider'];


export default setup;