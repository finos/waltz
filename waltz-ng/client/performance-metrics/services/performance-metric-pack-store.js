

function service($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/performance-metric/pack`;


    const getById = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(result => result.data);


    const findAllReferences = () => $http
        .get(BASE)
        .then(result => result.data);


    return {
        getById,
        findAllReferences
    };

}


service.$inject = [
    '$http',
    'BaseApiUrl'
];


export default service;

