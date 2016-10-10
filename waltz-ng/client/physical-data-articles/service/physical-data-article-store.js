function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/physical-data-article`;

    const findByAppId = (id) => $http
        .get(`${base}/application/${id}`)
        .then(r => r.data);


    const findById = (id) => $http
        .get(`${base}/id/${id}`)
        .then(r => r.data);


    return {
        findByAppId,
        findById
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;