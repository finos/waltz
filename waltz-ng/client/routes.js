function configure($stateProvider, $urlRouterProvider) {

    $urlRouterProvider.when('', '/home');

    $stateProvider
        .state('main', {
            url: '/',
            views: {
                'header': {template: '<waltz-navbar></waltz-navbar>'},
                'sidebar': {template: '<waltz-sidebar></waltz-sidebar>'},
                'content': require('./welcome/welcome.js'),
                'footer': {template: require('./footer/footer.html')}
            }
        })
        .state('main.home', {
            url: 'home',
            views: {
                'content': {template: require('./welcome/welcome.html')}
            }
        });
}

configure.$inject = [
    '$stateProvider',
    '$urlRouterProvider'
];


function setupRoutes(module) {
    module.config(configure);
}



export default setupRoutes;