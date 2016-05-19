let loaderPromise = null;

function service($http, baseUrl) {

    const BASE = `${baseUrl}/source-data-rating`;


    const findAll = (force = false) => {
        if (loaderPromise && ! force) return loaderPromise;

        loaderPromise = $http
            .get(`${BASE}`)
            .then(result => result.data);

        return loaderPromise;
    };

    return {
        findAll
    };
}

service.$inject = ['$http', 'BaseApiUrl'];


export default service;
