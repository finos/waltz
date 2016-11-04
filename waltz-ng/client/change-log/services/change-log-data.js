
export default [
    '$http',
    'BaseApiUrl',
    ($http, BaseApiUrl) => {

        const BASE = `${BaseApiUrl}/change-log`;

        const findByEntityReference = (kind, id, limit = 30) =>
            $http.get(`${BASE}/${kind}/${id}`, { params: { limit }})
                .then(result => result.data);

        const findForUserName = (userName) =>
            $http.get(`${BASE}/user/${userName}`)
                .then(r => r.data);


        return {
            findByEntityReference,
            findForUserName
        };
    }
];
