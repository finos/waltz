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

    return {
        findAll
    };
}

service.$inject = ['$http', 'BaseApiUrl', '$q'];


export default service;
