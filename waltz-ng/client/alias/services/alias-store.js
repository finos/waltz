import {checkIsEntityRef, checkIsStringList} from "../../common/checks";


function service($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/entity/alias`;

    const update = (entityRef, aliases = []) => {
        checkIsEntityRef(entityRef);
        checkIsStringList(aliases);

        return $http
            .post(`${BASE}/${entityRef.kind}/${entityRef.id}`, aliases)
            .then(r => r.data);
    };

    const getForEntity = () => {};


    return {
        update,
        getForEntity
    }
}


service.$inject = [
    '$http',
    'BaseApiUrl'
];

export default service;