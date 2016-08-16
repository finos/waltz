import {loadDataTypes, dataTypeByCodeResolver, dataTypeByIdResolver} from "./resolvers";

const baseState = {
    resolve: {
        dataTypes: loadDataTypes
    }
};


const listState = {
    url: 'data-types',
    views: {'content@': require('./data-type-list') }
};


const viewByCodeState = {
    url: 'data-types/code/{code}',
    views: {'content@': require('./data-type-view') },
    resolve: {dataType: dataTypeByCodeResolver }
};


const viewState = {
    url: 'data-types/{id:int}',
    views: {'content@': require('./data-type-view') },
    resolve: {dataType: dataTypeByIdResolver }
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