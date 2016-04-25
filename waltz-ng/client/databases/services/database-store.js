
function service(http, baseUrl) {

    const BASE = `${baseUrl}/database`;

    const findByAppId = (appId) =>
        http.get(`${BASE}/app/${appId}`)
            .then(result => result.data);

    const findByAppIds = (appIds) =>
        http.post(`${BASE}/app`, appIds)
            .then(result => result.data);

    return {
        findByAppId,
        findByAppIds
    };
}

service.$inject = ['$http', 'BaseApiUrl'];


export default service;
