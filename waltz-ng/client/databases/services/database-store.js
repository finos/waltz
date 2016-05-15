
function service(http, baseUrl) {

    const BASE = `${baseUrl}/database`;

    const findByAppId = (appId) =>
        http.get(`${BASE}/app/${appId}`)
            .then(result => result.data);

    const findBySelector = (id, kind, scope='CHILDREN') =>
        http.post(`${BASE}`, { scope, entityReference: { id, kind }})
            .then(result => result.data);

    const findStatsForSelector = (id, kind, scope='CHILDREN') =>
        http.post(`${BASE}/stats`, { scope, entityReference: { id, kind }})
            .then(result => result.data);

    return {
        findByAppId,
        findBySelector,
        findStatsForSelector
    };
}

service.$inject = ['$http', 'BaseApiUrl'];


export default service;
