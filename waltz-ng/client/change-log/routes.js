import ChangeLogView from "./view";


const baseState = {
    url: 'change-log'
};


const entityViewState = {
    url: '/view/entity/:kind/:id?name',
    views: { 'content@': ChangeLogView }
};


function setup($stateProvider) {
    $stateProvider
        .state('main.change-log', baseState)
        .state('main.change-log.view', entityViewState);
}


setup.$inject = ['$stateProvider'];


export default setup;