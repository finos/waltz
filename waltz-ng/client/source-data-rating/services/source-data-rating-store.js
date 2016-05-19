function service(http, baseUrl) {

    const BASE = `${baseUrl}/source-data-rating`;


    const findAll = () => http
        .get(`${BASE}`)
        .then(result => result.data);


    return {
        findAll
    };
}

service.$inject = ['$http', 'BaseApiUrl'];


export default service;
