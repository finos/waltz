
function store($http,
               $window,
               BaseApiUrl) {
    const BASE = `${BaseApiUrl}/user-agent-info`;

    const findForUser = (userName, limit = 10) => $http
        .get(`${BASE}/user/${userName}`, { params: { limit } })
        .then(r => r.data);

    const save = () => {

        const browserInfo = {
            operatingSystem: $window.navigator.platform,
            resolution: `${ $window.screen.width }x${ $window.screen.height }`
        };

        return $http
            .post(BASE, browserInfo)
            .then(r => r.data);
    };

    return {
        findForUser,
        save
    };
}

store.$inject = [
    '$http',
    '$window',
    'BaseApiUrl'
];


export default store;
