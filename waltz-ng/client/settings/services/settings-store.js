function service(http, baseUrl) {

    const BASE = `${baseUrl}/settings`;

    const findAll = (assetCode) =>
        http.get(`${BASE}`)
            .then(result => result.data);

    return {
        findAll
    };
}

service.$inject = ['$http', 'BaseApiUrl'];


export default service;
