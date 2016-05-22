const personHome = {
    url: 'person',
    views: {'content@': require('./person-home') }
};

const personView = {
    url: '/:empId',
    views: {'content@': require('./person-view') }
};


function setup($stateProvider) {

    $stateProvider
        .state('main.person', personHome)
        .state('main.person.view', personView);
}

setup.$inject = ['$stateProvider'];


export default setup;