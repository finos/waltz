import {checkIsApplicationIdSelector} from "../../common/checks";


function service($http,
                 baseUrl) {

    const BASE = `${baseUrl}/data-type-usage`;

    const findForEntity = (kind, id) => $http
        .get(`${BASE}/entity/${kind}/${id}`)
        .then(result => result.data);

    const findForDataType = (type) => $http
        .get(`${BASE}/type/${type}`)
        .then(result => result.data);

    const findForSelector = (selector) => {
        checkIsApplicationIdSelector(selector);
        return $http
            .post(`${BASE}/selector`, selector)
            .then(result => result.data);
    };

    return {
        findForEntity,
        findForDataType,
        findForSelector
    };
}

service.$inject = [
    '$http',
    'BaseApiUrl'
];


export default service;
