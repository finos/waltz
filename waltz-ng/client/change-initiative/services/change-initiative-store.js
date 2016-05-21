
function service($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/change-initiative`;

    
    const findByRef = (kind, id) => $http
            .get(`${BASE}/ref/${kind}/${id}`)
            .then(r => r.data);

    
    const getById = (id) => $http
            .get(`${BASE}/id/${id}`)
            .then(r => r.data);


    return {
        findByRef,
        getById
    }
}

service.$inject = ['$http', 'BaseApiUrl'];

export default service;
