import namedSettings from "../named-settings";


function hasRole(userService, role) {
    return userService
        .whoami()
        .then(user => userService.hasRole(user, role));
}


function isBetaServer(settingsStore) {

    return settingsStore
        .findAll()
        .then(settings => settingsStore.findOrDefault(settings, namedSettings.betaServer, false))
        .then(isBeta => { return isBeta === 'true'; });
}


function service($q, settingsStore, userService) {

    const getNagEnabled = () => {

        return $q.all( [ hasRole(userService, 'BETA_TESTER'), isBetaServer(settingsStore) ] )
            .then( ([isBetaTester, isBetaServer]) => !isBetaTester && isBetaServer) ;
    };


    const getNagMessage = () => {
        return settingsStore
            .findAll()
            .then(settings => settingsStore.findOrDefault(settings, namedSettings.betaNagMessage, ""));
    };


    return {
        getNagEnabled,
        getNagMessage
    };
}


service.$inject = [
    '$q',
    'SettingsStore',
    'UserService'
];


export default service;
