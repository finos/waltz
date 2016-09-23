import namedSettings from "../named-settings";


function hasRole(userService, role) {
    return userService
        .whoami()
        .then(user => userService.hasRole(user, role));
}


function isBetaServer(settingsService) {

    return settingsService
        .findAll()
        .then(settings => settingsService.findOrDefault(settings, namedSettings.betaEnvironment, false))
        .then(isBeta => { return isBeta === 'true'; });
}


function getNagEnabled($q, userService, settingsService) {

    return $q.all( [ hasRole(userService, 'BETA_TESTER'), isBetaServer(settingsService) ] )
        .then( ([isBetaTester, isBetaServer]) => !isBetaTester && isBetaServer) ;
}


function getNagMessage(settingsService) {
    return settingsService
        .findAll()
        .then(settings => settingsService.findOrDefault(settings, namedSettings.betaNagMessage, ""));
}


function service($q, settingsService, userService) {

    const setupNag = (nagFunction) => {
        getNagEnabled($q, userService, settingsService)
            .then(nagEnabled => {
                if(nagEnabled) {
                    getNagMessage(settingsService)
                        .then(nagMessage => nagFunction(nagMessage));
                }
            });
    };


    return {
        setupNag
    };
}


service.$inject = [
    '$q',
    'SettingsService',
    'UserService'
];


export default service;
