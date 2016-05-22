
function setup($stateProvider){
    $stateProvider
        .state('main.settings', {
            url: 'settings',
            views: { 'content@': require('./settings-view') }
        });
}

setup.$inject = ['$stateProvider'];


export default setup;