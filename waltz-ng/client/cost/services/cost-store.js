
export function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/cost`;

    const findByEntityReference = (ref) =>
        $http
            .get(`${BASE}/entity/kind/${ref.kind}/id/${ref.id}`)
            .then(r => r.data);

    const findBySelector = (targetKind, selectionOptions) =>
        $http
            .post(`${BASE}/target-kind/${targetKind}`, selectionOptions)
            .then(r => r.data);

    const findByCostKindAndSelector = (costKindId, targetKind, selectionOptions) =>
        $http
            .post(`${BASE}/cost-kind/${costKindId}/target-kind/${targetKind}`, selectionOptions)
            .then(r => r.data);


    return {
        findByEntityReference,
        findBySelector,
        findByCostKindAndSelector
    };
}

store.$inject = [
    '$http',
    'BaseApiUrl',
];

export const serviceName = 'CostStore';

export const CostStore_API = {
    findByEntityReference: {
        serviceName,
        serviceFnName: 'findByEntityReference',
        description: 'executes findByEntityReference'
    },
    findBySelector: {
        serviceName,
        serviceFnName: 'findBySelector',
        description: 'executes findBySelector [targetKind, selectionOptions]'
    },
    findByCostKindAndSelector: {
        serviceName,
        serviceFnName: 'findByCostKindAndSelector',
        description: 'executes findByCostKindAndSelector [costKindId, targetKind, selectionOptions]'
    },
};

export default {
    serviceName,
    store
}
