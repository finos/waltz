import {checkIsApplicationIdSelector, checkIsEntityRef} from "../../common/checks";


function service($http,
                 baseUrl) {

    const BASE = `${baseUrl}/data-flow-decorator`;

    const findBySelectorAndKind = (selector, kind = 'DATA_TYPE') => {
        checkIsApplicationIdSelector(selector);
        return $http
            .post(`${BASE}/kind/${kind}`, selector)
            .then(result => result.data);
    };

    return {
        findBySelectorAndKind
    };
}

service.$inject = [
    '$http',
    'BaseApiUrl'
];


export default service;
