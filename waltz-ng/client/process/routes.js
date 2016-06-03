import ListView from "./list";


const baseState = {
    url: 'process'
};

const listState = {
    url: '/list',
    views: {'content@': ListView}
};



function setup($stateProvider) {
    $stateProvider
        .state('main.process', baseState)
        .state('main.process.list', listState);
}

setup.$inject = ['$stateProvider'];


export default setup;