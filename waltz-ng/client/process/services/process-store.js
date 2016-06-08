function service($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/process`;


    const getById = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(result => result.data);


    const findAll = () => $http
        .get(BASE)
        .then(result => result.data);


    const findSupportingCapabilities = (id) => $http
        .get(`${BASE}/id/${id}/capabilities`)
        .then(result => result.data);


    const findForApplication = (id) => $http
        .get(`${BASE}/application/${id}`)
        .then(result => result.data);


    const findForCapability = (id) => $http
        .get(`${BASE}/capability/${id}`)
        .then(result => result.data);


    return {
        getById,
        findAll,
        findForApplication,
        findForCapability,
        findSupportingCapabilities
    };

}


service.$inject = [
    '$http',
    'BaseApiUrl'
];


export default service;

