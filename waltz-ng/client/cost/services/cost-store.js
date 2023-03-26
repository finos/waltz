
export function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/cost`;

    const findByEntityReference = (ref) =>
        $http
            .get(`${BASE}/entity/kind/${ref.kind}/id/${ref.id}`)
            .then(r => r.data);

    const findBySelector = (targetKind, year, selectionOptions) =>
        $http
            .post(`${BASE}/target-kind/${targetKind}/year/${year}`, selectionOptions)
            .then(r => r.data);

    const summariseByCostKindAndSelector = (costKindId,
                                            targetKind,
                                            year,
                                            selectionOptions) =>
        $http
            .post(`${BASE}/cost-kind/${costKindId}/target-kind/${targetKind}/summary/year/${year}`, selectionOptions)
            .then(r => r.data);

    return {
        findByEntityReference,
        findBySelector,
        summariseByCostKindAndSelector
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
        description: 'executes findBySelector [targetKind, year, selectionOptions]'
    },
    summariseByCostKindAndSelector: {
        serviceName,
        serviceFnName: 'summariseByCostKindAndSelector',
        description: 'executes summariseByCostKindAndSelector [costKindId, targetKind, selectionOptions]'
    },
};

export default {
    serviceName,
    store
}
