function service(http, base) {
    const baseUrl = `${base}/software-catalog`;

    const findByAppIds = (ids = []) =>
        http.post(`${baseUrl}/apps`, ids)
            .then(r => r.data);

    return {
        findByAppIds
    };
}

service.$inject = ['$http', 'BaseApiUrl'];


export default service;
