const baseState = {
};


const viewState = {
    url: 'physical-flow/{id:int}',
    views: {'content@': require('./physical-flow-view') },
};



const registrationState = {
    url: 'physical-flow/registration/{kind:string}/{id:int}',
    views: {'content@': require('./physical-flow-registration') },
};


function setup($stateProvider) {

    $stateProvider
        .state('main.physical-flow', baseState)
        .state('main.physical-flow.registration', registrationState)
        .state('main.physical-flow.view', viewState);
}


setup.$inject = ['$stateProvider'];


export default setup;