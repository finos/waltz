function service($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/process`;


    const getById = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(result => result.data);


    const findAll = () => $http
        .get(BASE)
        .then(result => result.data);


    return {
        getById,
        findAll
    };

}


service.$inject = [
    '$http',
    'BaseApiUrl'
];


export default service;

