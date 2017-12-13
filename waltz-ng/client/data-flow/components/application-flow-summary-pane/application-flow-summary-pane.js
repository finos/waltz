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

import _ from 'lodash';
import { initialiseData } from '../../../common';

import { CORE_API } from "../../../common/services/core-api-utils";
import { findUnknownDataType } from '../../../data-types/data-type-utils';
import { categorizeDirection } from "../../../logical-flow/logical-flow-utils";
import { nest } from "d3-collection";

import template from './application-flow-summary-pane.html';


const bindings = {
    parentEntityRef: '<'
};


const initialState = {
    authoritativeCols: [
        'PRIMARY',
        'SECONDARY',
        'DISCOURAGED',
        'NO_OPINION'
    ],
    visibility: {
        stats: false
    }
};


function enrichDecorators(parentEntityRef, unknownDataTypeId, logicalFlows = [], decorators = []) {
    const logicalFlowsById = _.keyBy(logicalFlows, 'id');
    const isKnownDataType = (decorator) => decorator.decoratorEntity.id !== unknownDataTypeId;

    return _
        .chain(decorators)
        .filter(d => d.decoratorEntity.kind === 'DATA_TYPE')
        .map(d => {
            const flow = logicalFlowsById[d.dataFlowId];
            return {
                decorator: d,
                logicalFlow: flow,
                direction: categorizeDirection(flow, parentEntityRef),
                mappingStatus: isKnownDataType(d) ? 'KNOWN': 'UNKNOWN'
            };
        })
        .value();
}


function calcStats(enrichedDecorators = []) {
    const byDirectionAndMappingStatus = nest()
        .key(d => d.direction)
        .key(d => d.mappingStatus)
        .object(enrichedDecorators);

    const byDirectionAndAuthoritativeness = nest()
        .key(d => d.direction)
        .key(d => d.decorator.rating)
        .object(enrichedDecorators);

    const chartData = nest()
        .key(d => d.direction)
        .key(d => d.mappingStatus)
        .key(d => d.decorator.rating)
        .rollup(xs => xs.length)
        .object(enrichedDecorators);

    return {
        byDirectionAndMappingStatus,
        byDirectionAndAuthoritativeness,
        chartData
    };
}


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const reload = (unknownDataTypeId) => {
        const selector = {
            entityReference: vm.parentEntityRef,
            scope: 'EXACT'
        };

        const logicalFlowPromise = serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.findByEntityReference,
                [vm.parentEntityRef])
            .then(r => r.data);

        const decorationPromise = serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowDecoratorStore.findBySelectorAndKind,
                [selector, 'DATA_TYPE'])
            .then(r => r.data);

        $q.all([logicalFlowPromise, decorationPromise])
            .then(([logicalFlows, decorators]) => {
                vm.enrichedDecorators = enrichDecorators(
                    vm.parentEntityRef,
                    Number(unknownDataTypeId),
                    logicalFlows,
                    decorators);

                vm.stats = calcStats(vm.enrichedDecorators);
            });
    };


    const loadUnknownDataType = () => {
        return serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => findUnknownDataType(r.data));
    };


    vm.$onInit = () => {
        loadUnknownDataType()
            .then(unknownDataType => reload(unknownDataType.id))
    }
}


controller.$inject = [
    '$q',
    'ServiceBroker',
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzApplicationFlowSummaryPane'
};
