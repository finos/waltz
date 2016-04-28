function service(http, base) {
    const baseUrl = `${base}/software-catalog`;

    const findByAppIds = (ids = []) =>
        http.post(`${baseUrl}/apps`, ids)
            .then(r => r.data);

    const findStatsForAppIds = (appIds) =>
        http.post(`${baseUrl}/apps/stats`, appIds)
            .then(result => result.data);

    return {
        findByAppIds,
        findStatsForAppIds
    };
}

service.$inject = ['$http', 'BaseApiUrl'];


export default service;
