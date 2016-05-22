import EditView from "./data-flow-edit";


function applicationResolver(ApplicationStore, $stateParams) {
    return ApplicationStore.getById($stateParams.id)
}


applicationResolver.$inject = [
    'ApplicationStore',
    '$stateParams'
];

const base = {
    url: 'data-flow'
};


const editState = {
    url: '/edit/:kind/:id',
    views: {'content@': EditView },
    resolve: {
        application: applicationResolver
    }
};



function setup($stateProvider) {
    $stateProvider
        .state('main.data-flow', base)
        .state('main.data-flow.edit', editState);
}


setup.$inject = [
    '$stateProvider'
];


export default setup;