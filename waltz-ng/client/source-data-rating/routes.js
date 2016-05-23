
function setup($stateProvider) {
    $stateProvider
        .state('main.source-data-ratings', {
            url: 'source-data-ratings',
            views: { 'content@': require('./list') }
        });
}

setup.$inject = ['$stateProvider'];


export default setup;