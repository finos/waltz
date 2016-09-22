function configureRoutes($stateProvider, $urlRouterProvider) {

    $urlRouterProvider.when('', '/home');

    $stateProvider
        .state('main', {
            url: '/',
            views: {
                'header': {template: '<waltz-navbar></waltz-navbar>'},
                'content': require('./welcome/welcome.js')
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


function configureBetaNagMessageNotification($rootScope,
                                             nagMessageService,
                                             notification) {

    const setupNagNotification = (message = "") => {
        $rootScope.$on('$stateChangeSuccess', () => {
            notification.info(message);
        });
    };


    nagMessageService
        .getNagEnabled()
        .then(nagEnabled => {
            if(nagEnabled) {
                nagMessageService
                    .getNagMessage()
                    .then(nagMessage => setupNagNotification(nagMessage));
            }
        });
}


configureBetaNagMessageNotification.$inject = [
    '$rootScope',
    'NagMessageService',
    'Notification'
];


function setup(module) {
    module.config(configureRoutes);
    module
        .run(configureScrollToTopOnChange)
        .run(configureBetaNagMessageNotification);
}



export default setup;