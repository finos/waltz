function service($http, base) {

    const findForUserId = (userId) =>
        $http.get(`${base}/user-contribution/user/${userId}/score`)
            .then(r => r.data);

    const findForDirects = (userId) =>
        $http.get(`${base}/user-contribution/user/${userId}/directs/score`)
            .then(r => r.data);

    const getLeaderBoard = () =>
        $http.get(`${base}/user-contribution/leader-board`)
            .then(r => r.data);

    return {
        findForUserId,
        findForDirects,
        getLeaderBoard
    };
}

service.$inject = [
    '$http',
    'BaseApiUrl'
];


export default service;
