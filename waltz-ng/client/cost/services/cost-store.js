
export function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/cost`;

    const findByEntityReference = (ref) =>
        $http
            .get(`${BASE}/entity/kind/${ref.kind}/id/${ref.id}`)
            .then(r => r.data);


    return {
        findByEntityReference
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
};
