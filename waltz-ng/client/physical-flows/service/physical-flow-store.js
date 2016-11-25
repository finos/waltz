import {checkIsEntityRef, checkIsIdSelector, checkIsCreatePhysicalFlowCommand} from "../../common/checks";


function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/physical-flow`;


    const findByEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${base}/entity/${ref.kind}/${ref.id}`)
            .then(r => r.data);
    };


    const findByProducerEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${base}/entity/${ref.kind}/${ref.id}/produces`)
            .then(r => r.data);
    };


    const findByConsumerEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${base}/entity/${ref.kind}/${ref.id}/consumes`)
            .then(r => r.data);
    };


    const findBySpecificationId = (id) => {
        return $http
            .get(`${base}/specification/${id}`)
            .then(r => r.data);
    };

    const getById = (id) => {
        return $http
            .get(`${base}/id/${id}`)
            .then(r => r.data);
    };


    const findBySelector = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${base}/selector`, options)
            .then(r => r.data);
    };


    const create = (cmd) => {
        checkIsCreatePhysicalFlowCommand(cmd);
        return $http
            .post(base, cmd)
            .then(r => r.data);
    };


    const searchReports = (query) => {
        return $http
            .get(`${base}/search-reports/${query}`)
            .then(r => r.data);
    };


    const deleteById = (id) => $http
            .delete(`${base}/${id}`)
            .then(r => r.data);


    return {
        findBySpecificationId,
        findByEntityReference,
        findByProducerEntityReference,
        findByConsumerEntityReference,
        findBySelector,
        getById,
        searchReports,
        create,
        deleteById
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;