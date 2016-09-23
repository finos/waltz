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


    const findOrDefault = (name = "", dflt = null) => {
        return findAll()
            .then(settings => {
                const found = _.find(settings, { name });
                return found ? found.value : dflt;
        });
    };


    const isDevModeEnabled = () => {
        return findOrDefault(namedSettings.devExtEnabled, false)
            .then(devModeEnabled => 'true' === devModeEnabled );
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
