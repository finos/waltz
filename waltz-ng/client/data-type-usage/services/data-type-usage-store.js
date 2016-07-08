import {checkIsApplicationIdSelector, checkIsEntityRef} from "../../common/checks";


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

    const save = (ref, dataTypeCode, usages = []) => {
        checkIsEntityRef(ref);
        if (usages.length == 0) return;
        return $http
            .post(`${BASE}/entity/${ref.kind}/${ref.id}/${dataTypeCode}`, usages)
            .then(r => r.data);
    };

    return {
        findForEntity,
        findForDataType,
        findForSelector,
        save
    };
}

service.$inject = [
    '$http',
    'BaseApiUrl'
];


export default service;
