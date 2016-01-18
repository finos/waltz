
const service = (http, root) => {

    const BASE = `${root}/asset-cost`;


    const findByCode = code =>
        http.get(`${BASE}/code/${code}`)
            .then(result => result.data);


    const findByOrgUnitTree = (id) =>
        http.get(`${BASE}/org-unit-tree/${id}`)
            .then(result => result.data);


    const findAppCostsByOrgUnitTree = (id) =>
        http.get(`${BASE}/app-cost/org-unit-tree/${id}`)
            .then(result => result.data);


    return {
        findByCode,
        findByOrgUnitTree,
        findAppCostsByOrgUnitTree
    };

};

service.$inject = ['$http', 'BaseApiUrl'];

export default service;