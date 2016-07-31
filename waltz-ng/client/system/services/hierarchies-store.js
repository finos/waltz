function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/entity-hierarchy`;

    const findTallies = () =>
        $http
            .get(`${BASE}/tallies`)
            .then(r => r.data);

    const buildForKind = (kind) =>
        $http
            .post(`${BASE}/build/${kind}`, {})
            .then(r => r.data);

    return {
        findTallies,
        buildForKind
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;
