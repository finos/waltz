const baseState = {
    url: 'physical-flow-lineage',
};


const editState = {
    url: '/{id:int}/edit',
    views: {'content@': require('./lineage-edit') },
};


function setup($stateProvider) {
    $stateProvider
        .state('main.physical-flow-lineage', baseState)
        .state('main.physical-flow-lineage.edit', editState);
}


setup.$inject = ['$stateProvider'];


export default setup;