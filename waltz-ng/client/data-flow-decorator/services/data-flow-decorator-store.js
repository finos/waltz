import {checkIsApplicationIdSelector} from "../../common/checks";


function service($http,
                 baseUrl) {

    const BASE = `${baseUrl}/data-flow-decorator`;

    const findBySelectorAndKind = (selector, kind = 'DATA_TYPE') => {
        checkIsApplicationIdSelector(selector);
        return $http
            .post(`${BASE}/kind/${kind}`, selector)
            .then(result => result.data);
    };

    /**
     * where command like:
     *    {
     *      flowId: <number>,
     *      addedDecorators: [<entityRef>],
     *      removedDecorators: [<entityRef>]
     *    }
     *
     * @param command
     * @returns {*|Promise.<FlowDecorator>}
     */
    const updateDecorators = (command) => {
        return $http
            .post(`${BASE}/${command.flowId}`, command)
            .then(r => r.data);
    };

    return {
        findBySelectorAndKind,
        updateDecorators
    };
}

service.$inject = [
    '$http',
    'BaseApiUrl'
];


export default service;
