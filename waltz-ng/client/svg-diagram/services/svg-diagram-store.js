
function service(http, base) {

    const findByKind = (kind) =>
        http.get(`${base}/svg-diagram/kind/${kind}`)
            .then(r => r.data);

    return {
        findByKind
    };
}


service.$inject = ['$http', 'BaseApiUrl'];

export default service;
