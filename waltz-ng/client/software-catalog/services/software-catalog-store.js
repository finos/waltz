function service(http, base) {
    const baseUrl = `${base}/software-catalog`;

    const findByAppIds = (ids = []) =>
        http.post(`${baseUrl}/apps`, ids)
            .then(r => r.data);

    const findStatsForSelector = (id, kind, scope = 'CHILDREN') =>
        http.post(`${baseUrl}/stats`, { scope, entityReference : { id, kind } })
            .then(result => result.data);

    return {
        findByAppIds,
        findStatsForSelector
    };
}

service.$inject = ['$http', 'BaseApiUrl'];


export default service;
