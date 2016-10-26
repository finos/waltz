import {checkIsIdSelector, checkIsEntityRef} from "../../common/checks";

function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/physical-specification`;


    const findByEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${base}/application/${ref.kind}/${ref.id}`)
            .then(r => r.data);
    };


    const findBySelector = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${base}/selector`, options)
            .then(r => r.data);
    };


    const findByDescribedLineage = () => {
        return $http
            .get(`${base}/lineage`)
            .then(r => r.data);
    };


    const getById = (id) => $http
        .get(`${base}/id/${id}`)
        .then(r => r.data);


    return {
        findByEntityReference,
        findBySelector,
        findByDescribedLineage,
        getById
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;