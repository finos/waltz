
export function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/cost-kind`;

    const findAll = () =>
        $http
            .get(`${BASE}`)
            .then(r => r.data);

    const findBySelector = (targetKind, selector) =>
        $http
            .post(`${BASE}/target-kind/${targetKind}/selector`, selector)
            .then(r => r.data);


    return {
        findAll,
        findBySelector
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
    findBySelector: {
        serviceName,
        serviceFnName: 'findBySelector',
        description: 'executes findBySelector, [targetKind, selector]'
    },
};

export default {
    serviceName,
    store
}
