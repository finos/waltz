import {checkIsEntityRef} from '../../common/checks';


function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/physical-data-flow`;

    const findByEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${base}/entity/${ref.kind}/${ref.id}`)
            .then(r => r.data);
    };

    const findByArticleId = (id) => {
        return $http
            .get(`${base}/article/${id}`)
            .then(r => r.data);
    };

    return {
        findByEntityReference,
        findByArticleId
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;