function service($http, baseUrl) {

    const BASE = `${baseUrl}/settings`;


    const findAll = () => {
        return $http.get(`${BASE}`)
            .then(result => result.data);
    };


    return {
        findAll
    };
}


service.$inject = [
    '$http',
    'BaseApiUrl'
];


export default service;
