import _ from "lodash";
import namedSettings from "../named-settings";


let settingsPromise = null;

function service(settingsStore) {

    const findAll = (force = false) => {
        if (force || settingsPromise == null) {
            settingsPromise = settingsStore.findAll();
        }
        return settingsPromise;
    };


    const findOrDefault = (settings, name = "", dflt = null) => {
        if (!_.isArray(settings)) {
            throw "First argument to findOrDefault must be an array of settings";
        }
        const found = _.find(settings, { name });
        return found ? found.value : dflt;
    };


    const isDevModeEnabled = (settings) => {
        const devModeEnabled = findOrDefault(settings, namedSettings.devExtEnabled, false);
        return 'true' === devModeEnabled
    };


    return {
        findAll,
        findOrDefault,
        isDevModeEnabled
    };
}

service.$inject = [
    'SettingsStore'
];


export default service;
