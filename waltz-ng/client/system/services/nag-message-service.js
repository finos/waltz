import namedSettings from "../named-settings";


function hasRole(userService, role) {
    return userService
        .whoami()
        .then(user => userService.hasRole(user, role));
}


function isBetaServer(settingsStore) {

    return settingsStore
        .findAll()
        .then(settings => settingsStore.findOrDefault(settings, namedSettings.betaEnvironment, false))
        .then(isBeta => { return isBeta === 'true'; });
}


function getNagEnabled($q, userService, settingsStore) {

    return $q.all( [ hasRole(userService, 'BETA_TESTER'), isBetaServer(settingsStore) ] )
        .then( ([isBetaTester, isBetaServer]) => !isBetaTester && isBetaServer) ;
}


function getNagMessage(settingsStore) {
    return settingsStore
        .findAll()
        .then(settings => settingsStore.findOrDefault(settings, namedSettings.betaNagMessage, ""));
}


function service($q, settingsStore, userService) {

    const setupNag = (nagFunction) => {
        getNagEnabled($q, userService, settingsStore)
            .then(nagEnabled => {
                if(nagEnabled) {
                    getNagMessage(settingsStore)
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
    'SettingsStore',
    'UserService'
];


export default service;
