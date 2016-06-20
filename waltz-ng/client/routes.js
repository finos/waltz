function configureRoutes($stateProvider, $urlRouterProvider) {

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

configureRoutes.$inject = [
    '$stateProvider',
    '$urlRouterProvider'
];


function configureScrollToTopOnChange($rootScope, $doc) {
    $rootScope.$on('$stateChangeSuccess', () => {
        $doc[0].body.scrollTop = 0;
        $doc[0].documentElement.scrollTop = 0;
    });
}

configureScrollToTopOnChange.$inject = [
    '$rootScope',
    '$document'
];


function setup(module) {
    module.config(configureRoutes);
    module.run(configureScrollToTopOnChange);
}



export default setup;