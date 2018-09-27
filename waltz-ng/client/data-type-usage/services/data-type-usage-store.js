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

import {checkIsEntityRef, checkIsIdSelector} from "../../common/checks";
import {isEmpty} from "../../common";
import _ from "lodash";


export function store($http,
                      baseUrl) {

    const BASE = `${baseUrl}/data-type-usage`;

    const findForEntity = (kind, id) => {
        const entityReference = _.isObject(kind)
            ? kind
            : { kind, id };

        checkIsEntityRef(entityReference);

        return $http
            .get(`${BASE}/entity/${entityReference.kind}/${entityReference.id}`)
            .then(result => result.data);
    };

    const findForDataTypeSelector = (selector) => {
        checkIsIdSelector(selector);
        return $http
            .post(`${BASE}/type/`, selector)
            .then(result => result.data);
    };

    /**
     * returns tallies by usage-kind
     * @param selector
     */
    const calculateStats = (selector) => {
        checkIsIdSelector(selector);
        return $http
            .post(`${BASE}/type/stats`, selector)
            .then(result => result.data);
    };

    const findForSelector = (selector) => {
        checkIsIdSelector(selector);
        return $http
            .post(`${BASE}/selector`, selector)
            .then(result => result.data);
    };

    /**
     * selector -> { dataTypeId -> [ <entity_ref>... ] }
     *
     * @param selector
     * @returns {*|Promise.<result.data|{}>}
     */
    const findForUsageKindByDataTypeIdSelector = (usageKind, selector) => {
        checkIsIdSelector(selector);
        return $http
            .post(`${BASE}/usage-kind/${usageKind}`, selector)
            .then(result => result.data);
    };

    const save = (ref, dataTypeId, usages = []) => {
        checkIsEntityRef(ref);
        if (isEmpty(usages)) return;
        return $http
            .post(`${BASE}/entity/${ref.kind}/${ref.id}/${dataTypeId}`, usages)
            .then(r => r.data);
    };

    const recalculateAll = () =>
        $http
            .get(`${BASE}/calculate-all/application`)
            .then(r => r.data);

    return {
        findForEntity,
        findForDataTypeSelector,
        findForUsageKindByDataTypeIdSelector,
        calculateStats,
        findForSelector,
        recalculateAll,
        save
    };
}

store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = 'DataTypeUsageStore';


export const DataTypeUsageStore_API = {
    findForEntity: {
        serviceName,
        serviceFnName: 'findForEntity',
        description: 'findForEntity'
    },
    findForDataTypeSelector: {
        serviceName,
        serviceFnName: 'findForDataTypeSelector',
        description: 'findForDataTypeSelector'
    },
    findForUsageKindByDataTypeIdSelector: {
        serviceName,
        serviceFnName: 'findForUsageKindByDataTypeIdSelector',
        description: 'executes findForUsageKindByDataTypeIdSelector'
    },
    calculateStats: {
        serviceName,
        serviceFnName: 'calculateStats',
        description: 'executes calculateStats'
    },
    findForSelector: {
        serviceName,
        serviceFnName: 'findForSelector',
        description: 'executes findForSelector'
    },
    recalculateAll: {
        serviceName,
        serviceFnName: 'recalculateAll',
        description: 'executes recalculateAll'
    },
    save: {
        serviceName,
        serviceFnName: 'save',
        description: 'executes save'
    }
};