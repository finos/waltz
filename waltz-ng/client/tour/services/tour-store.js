
function store($http, base) {

    const BASE = `${base}/tour`;

    const findForKey = (key) =>
        $http.get(`${BASE}/${key}`)
            .then(r => r.data);

    return {
        findForKey
    };

}


store.$inject = ['$http', 'BaseApiUrl'];


export default store;