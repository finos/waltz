function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/entity-hierarchy`;

    const findTallies = () =>
        $http
            .get(`${BASE}/tallies`)
            .then(r => r.data);

    const findRootTallies = () =>
        $http
            .get(`${BASE}/root-tallies`)
            .then(r => r.data);

    const findRoots = (kind) =>
        $http
            .get(`${BASE}/roots/${kind}`)
            .then(r => r.data);

    const buildForKind = (kind) =>
        $http
            .post(`${BASE}/build/${kind}`, {})
            .then(r => r.data);

    return {
        findTallies,
        findRootTallies,
        findRoots,
        buildForKind
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;
