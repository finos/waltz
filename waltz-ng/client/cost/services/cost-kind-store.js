
export function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/cost-kind`;

    const findAll = () =>
        $http
            .get(`${BASE}`)
            .then(r => r.data);


    return {
        findAll
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
};

export default {
    serviceName,
    store
}
