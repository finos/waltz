
function service($http, baseUrl) {

    const BASE = `${baseUrl}/end-user-application`;

    const findBySelector = (selector) =>
        $http
            .post(`${BASE}/selector`, selector)
            .then(result => result.data);

    const countByOrganisationalUnit = () => $http
        .get(`${BASE}/count-by/org-unit`)
        .then(result => result.data);

    return {
        findBySelector,
        countByOrganisationalUnit
    };
}

service.$inject = ['$http', 'BaseApiUrl'];


export default service;
