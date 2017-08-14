const baseState = {
    url: 'attestation'
};

const instanceBaseState = {
    url: '/instance'
};

const instanceUserState = {
    url: '/user',
    views: {'content@': require('./attestation-instance-list-user-view')}
};


function setup($stateProvider) {
    $stateProvider
        .state('main.attestation', baseState)
        .state('main.attestation.instance', instanceBaseState)
        .state('main.attestation.instance.user', instanceUserState)
    ;
}


setup.$inject = ['$stateProvider'];


export default setup;