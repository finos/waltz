import {checkIsEntityRef} from '../../common/checks';


function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/physical-data-flow`;

    const findByEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${base}/${ref.kind}/${ref.id}`)
            .then(r => r.data);
    };

    return {
        findByEntityReference
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;