import {checkIsIdSelector} from '../../common/checks';

function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/physical-specification`;


    const findByAppId = (id) => $http
        .get(`${base}/application/${id}`)
        .then(r => r.data);


    const findBySelector = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${base}/selector`, options)
            .then(r => r.data);
    };


    const getById = (id) => $http
        .get(`${base}/id/${id}`)
        .then(r => r.data);


    return {
        findByAppId,
        findBySelector,
        getById
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;