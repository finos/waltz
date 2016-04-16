import _ from "lodash";
import namedSettings from "../named-settings";


let settingsCache = null;

function service(http, baseUrl, $q) {


    const BASE = `${baseUrl}/settings`;


    const findAll = (force = false) => {
        if (settingsCache == null || force) {
            return http.get(`${BASE}`)
                .then(result => result.data)
                .then(settings => settingsCache = settings);
        } else {
            return $q.when(settingsCache);
        }
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

service.$inject = ['$http', 'BaseApiUrl', '$q'];


export default service;
