
export default [
    '$http',
    'BaseApiUrl',
    ($http, BaseApiUrl) => {

        const BASE = `${BaseApiUrl}/change-log`;

        const findByEntityReference = (kind, id) =>
            $http.get(`${BASE}/${kind}/${id}`)
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
