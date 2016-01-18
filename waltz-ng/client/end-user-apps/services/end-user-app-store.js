
function service(http, baseUrl) {

    const BASE = `${baseUrl}/end-user-application`;

    const findByOrgUnitTree = (ouId) =>
        http.get(`${BASE}/org-unit-tree/${ouId}`)
            .then(result => result.data);

    return {
        findByOrgUnitTree
    };
}

service.$inject = ['$http', 'BaseApiUrl'];


export default service;
