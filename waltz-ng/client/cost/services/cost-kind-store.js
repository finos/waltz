
export function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/cost-kind`;

    const findAll = () =>
        $http
            .get(`${BASE}`)
            .then(r => r.data);

    const findExistingBySelector = (targetKind, selector) =>
        $http
            .post(`${BASE}/target-kind/${targetKind}/selector`, selector)
            .then(r => r.data);


    return {
        findAll,
        findExistingBySelector
    };
}

store.$inject = [
    '$http',
    'BaseApiUrl',
];

export const serviceName = 'CostKindStore';

export const CostKindStore_API = {
    findAll: {
        serviceName,
        serviceFnName: 'findAll',
        description: 'executes findAll'
    },
    findExistingBySelector: {
        serviceName,
        serviceFnName: 'findExistingBySelector',
        description: 'executes findExistingBySelector, [targetKind, selector]'
    },
};

export default {
    serviceName,
    store
}
