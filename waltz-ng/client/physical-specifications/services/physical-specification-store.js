import {checkIsIdSelector, checkIsEntityRef} from "../../common/checks";

function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/physical-specification`;


    const findByEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${base}/application/${ref.kind}/${ref.id}`)
            .then(r => r.data);
    };


    const findByProducerEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${base}/application/${ref.kind}/${ref.id}/produces`)
            .then(r => r.data);
    };


    const findByConsumerEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${base}/application/${ref.kind}/${ref.id}/consumes`)
            .then(r => r.data);
    };


    const findBySelector = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${base}/selector`, options)
            .then(r => r.data);
    };


    const getById = (id) => $http
        .get(`${base}/id/${id}`)
        .then(r => r.data);


    const deleteById = (id) => $http
            .delete(`${base}/${id}`)
            .then(r => r.data);


    return {
        findByEntityReference,
        findByProducerEntityReference,
        findByConsumerEntityReference,
        findBySelector,
        getById,
        deleteById
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;