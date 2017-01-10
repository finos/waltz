/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
