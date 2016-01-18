export default [
    '$http',
    'BaseApiUrl',
    ($http, BaseApiUrl) => {

        const BASE = `${BaseApiUrl}/app-view`;

        const getById = (id) => $http
            .get(`${BASE}/${id}`)
            .then(result => result.data);

        return {
            getById
        };

    }
];
