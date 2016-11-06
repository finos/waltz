import EditView from "./logical-flow-edit";


function applicationResolver(ApplicationStore, $stateParams) {
    return ApplicationStore.getById($stateParams.id)
}


applicationResolver.$inject = [
    'ApplicationStore',
    '$stateParams'
];

const base = {
    url: 'logical-flow'
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
        .state('main.logical-flow', base)
        .state('main.logical-flow.edit', editState);
}


setup.$inject = [
    '$stateProvider'
];


export default setup;