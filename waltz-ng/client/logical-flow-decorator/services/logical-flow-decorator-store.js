/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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


export function store($http,
                      baseUrl) {

    const BASE = `${baseUrl}/logical-flow-decorator`;

    const findBySelectorAndKind = (selector, kind = 'DATA_TYPE') => {
        checkIsApplicationIdSelector(selector);
        return $http
            .post(`${BASE}/selector/kind/${kind}`, selector)
            .then(result => result.data);
    };


    const findByFlowIdsAndKind = (flowIds = [], kind = 'DATA_TYPE') => {
        return $http
            .post(`${BASE}/flow-ids/kind/${kind}`, flowIds)
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
     * where command like:
     *    [{
     *      flowId: <number>,
     *      addedDecorators: [<entityRef>],
     *      removedDecorators: [<entityRef>]
     *    }]
     *
     * @param command
     * @returns {*|Promise.<[FlowDecorator]>}
     */
    const updateDecoratorsBatch = (commands) => {
        return $http
            .post(`${BASE}/batch`, commands);
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
    const summarizeInboundBySelector = (selector) => {
        checkIsApplicationIdSelector(selector);
        return $http.post(`${BASE}/summarize-inbound`, selector)
            .then(r => r.data);
    };

    const summarizeOutboundBySelector = (selector) => {
        checkIsApplicationIdSelector(selector);
        return $http.post(`${BASE}/summarize-outbound`, selector)
            .then(r => r.data);
    };

    const summarizeInboundForAll = () => {
        return $http.get(`${BASE}/summarize-inbound`)
            .then(r => r.data);
    };

    return {
        findBySelectorAndKind,
        findBySelector,
        findByFlowIdsAndKind,
        updateDecorators,
        updateDecoratorsBatch,
        summarizeInboundBySelector,
        summarizeOutboundBySelector,
        summarizeInboundForAll
    };
}

store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = "LogicalFlowDecoratorStore";


export const LogicalFlowDecoratorStore_API = {
    findBySelectorAndKind: {
        serviceName,
        serviceFnName: 'findBySelectorAndKind',
        description: 'executes findBySelectorAndKind'
    },
    findBySelector: {
        serviceName,
        serviceFnName: 'findBySelector',
        description: 'executes findBySelector'
    },
    findByFlowIdsAndKind: {
        serviceName,
        serviceFnName: 'findByFlowIdsAndKind',
        description: 'executes findByFlowIdsAndKind - arg1: [flowIds], arg2: decoratorKind'
    },
    updateDecorators: {
        serviceName,
        serviceFnName: 'updateDecorators',
        description: 'executes updateDecorators'
    },
    updateDecoratorsBatch: {
        serviceName,
        serviceFnName: 'updateDecoratorsBatch',
        description: 'executes updateDecoratorsBatch'
    },
    summarizeInboundBySelector: {
        serviceName,
        serviceFnName: 'summarizeInboundBySelector',
        description: 'executes summarizeInboundBySelector - arg1: app_selector'
    },
    summarizeOutboundBySelector: {
        serviceName,
        serviceFnName: 'summarizeOutboundBySelector',
        description: 'executes summarizeOutboundBySelector - arg1: app_selector'
    },
    summarizeInboundForAll: {
        serviceName,
        serviceFnName: 'summarizeInboundForAll',
        description: 'executes summarizeForAll (no args)'
    }
};
