function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/data-article`;

    const findByAppId = (id) => $http
        .get(`${base}/application/${id}`)
        .then(r => r.data);

    return {
        findByAppId
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;