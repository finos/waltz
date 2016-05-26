
function service($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/change-initiative`;


    const findByRef = (kind, id) => $http
            .get(`${BASE}/ref/${kind}/${id}`)
            .then(r => r.data);


    const getById = (id) => $http
            .get(`${BASE}/id/${id}`)
            .then(r => r.data);


    const findRelatedForId = (id) => $http
            .get(`${BASE}/id/${id}/related`)
            .then(r => r.data);


    const search = (query) => $http
            .get(`${BASE}/search/${query}`)
            .then(r => r.data);


    return {
        findByRef,
        findRelatedForId,
        getById,
        search
    }
}

service.$inject = ['$http', 'BaseApiUrl'];

export default service;
