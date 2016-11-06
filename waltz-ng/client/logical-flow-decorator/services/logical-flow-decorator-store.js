import {checkIsApplicationIdSelector, checkIsIdSelector} from "../../common/checks";


function service($http,
                 baseUrl) {

    const BASE = `${baseUrl}/data-flow-decorator`;

    const findBySelectorAndKind = (selector, kind = 'DATA_TYPE') => {
        checkIsApplicationIdSelector(selector);
        return $http
            .post(`${BASE}/kind/${kind}`, selector)
            .then(result => result.data);
    };


    const findBySelector = (selector) => {
        checkIsIdSelector(selector);
        return $http
            .post(`${BASE}/selector`, selector)
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

    /**
     * Given a selector returns a promising giving results like:
     *
     *      [
     *          { decoratorEntityReference : <entityRef>,
     *            rating: PRIMARY | SECONDARY | NO_OPINION | DISCOURAGED,
     *            count: <number>
     *          }
     *      ]
     *
     * @param selector
     * @returns {*|Promise.<TResult>}
     */
    const summarizeBySelector = (selector) => {
        checkIsApplicationIdSelector(selector);
        return $http.post(`${BASE}/summarize`, selector)
            .then(r => r.data);
    };

    return {
        findBySelectorAndKind,
        findBySelector,
        updateDecorators,
        summarizeBySelector
    };
}

service.$inject = [
    '$http',
    'BaseApiUrl'
];


export default service;
