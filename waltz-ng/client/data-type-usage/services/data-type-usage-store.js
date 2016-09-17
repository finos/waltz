import {checkIsIdSelector, checkIsEntityRef} from "../../common/checks";


function service($http,
                 baseUrl) {

    const BASE = `${baseUrl}/data-type-usage`;

    const findForEntity = (kind, id) => $http
        .get(`${BASE}/entity/${kind}/${id}`)
        .then(result => result.data);

    const findForDataTypeSelector = (selector) => {
        checkIsIdSelector(selector);
        return $http
            .post(`${BASE}/type/`, selector)
            .then(result => result.data);
    };

    /**
     * returns tallies by usage-kind
     * @param selector
     */
    const calculateStats = (selector) => {
        checkIsIdSelector(selector);
        return $http
            .post(`${BASE}/type/stats`, selector)
            .then(result => result.data);
    };

    const findForSelector = (selector) => {
        checkIsIdSelector(selector);
        return $http
            .post(`${BASE}/selector`, selector)
            .then(result => result.data);
    };

    /**
     * selector -> { dataTypeId -> [ <entity_ref>... ] }
     *
     * @param selector
     * @returns {*|Promise.<result.data|{}>}
     */
    const findForUsageKindByDataTypeIdSelector = (usageKind, selector) => {
        checkIsIdSelector(selector);
        return $http
            .post(`${BASE}/usage-kind/${usageKind}`, selector)
            .then(result => result.data);
    };

    const save = (ref, dataTypeCode, usages = []) => {
        checkIsEntityRef(ref);
        if (usages.length == 0) return;
        return $http
            .post(`${BASE}/entity/${ref.kind}/${ref.id}/${dataTypeCode}`, usages)
            .then(r => r.data);
    };

    const recalculateAll = () =>
        $http
            .get(`${BASE}/calculate-all/application`)
            .then(r => r.data);

    return {
        findForEntity,
        findForDataTypeSelector,
        findForUsageKindByDataTypeIdSelector,
        calculateStats,
        findForSelector,
        recalculateAll,
        save
    };
}

service.$inject = [
    '$http',
    'BaseApiUrl'
];


export default service;
