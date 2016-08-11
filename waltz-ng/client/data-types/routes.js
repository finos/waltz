const baseState = {
    resolve: {
        dataTypes: [
            'DataTypesService',
            (dataTypesService) => dataTypesService.loadDataTypes()
        ]
    }
};


const listState = {
    url: 'data-types',
    views: {'content@': require('./data-type-list') }
};


const viewByCodeState = {
    url: 'data-types/code/{code}',
    views: {'content@': require('./data-type-view') },
    redirectTo: 'main.data-type.view',
};


const viewState = {
    url: 'data-types/{id:int}',
    views: {'content@': require('./data-type-view') }
};


function setup($stateProvider) {

    $stateProvider
        .state('main.data-type', baseState)
        .state('main.data-type.list', listState)
        .state('main.data-type.code', viewByCodeState)
        .state('main.data-type.view', viewState);
}

setup.$inject = ['$stateProvider'];


export default setup;