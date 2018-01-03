/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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
import { nest } from "d3-collection";
import { CORE_API } from '../../../common/services/core-api-utils';
import { initialiseData } from '../../../common';
import { refToString, toEntityRef } from '../../../common/entity-utils';


import template from './bulk-logical-flow-parser.html';
import { invokeFunction } from '../../../common/index';


const bindings = {
    columnMappings: '<',
    sourceData: '<',
    onParseComplete: '<'
};


const initialState = {
    columnMappings: {},
    loading: false,
    parsedData: [],
    sourceData: [],

    onParseComplete: (event) => console.log('default onParseComplete handler for bulk-logical-flow-parser: ', event)
};


function resolveEntityRef(entitiesByIdentifier = {}, kind, identifier) {
    const search = _.toLower(identifier);
    const app = entitiesByIdentifier[search];
    const entityRef = app ? toEntityRef(app, kind) : null;

    return {
        identifier,
        entityRef
    };
}


function mapColumns(columnMappings = {}, sourceData = [], columnResolvers = {}) {
    const sourceKeys = _.keys(columnMappings);
    const mappedObjects = _.map(sourceData, sourceObj => {
        const targetObj = {};

        _.forEach(sourceKeys, sourceColumn => {
            const targetColumn = columnMappings[sourceColumn].name;
            const resolver = columnResolvers[targetColumn] || _.identity;
            targetObj[targetColumn] = resolver(sourceObj[sourceColumn]);
        });

        return targetObj;
    });
    return mappedObjects;
}


function findExistingLogicalFlowsAndDecorators(serviceBroker, sourcesAndTargets) {
    return serviceBroker
        .loadViewData(
            CORE_API.LogicalFlowStore.findBySourceAndTargetEntityReferences,
            [sourcesAndTargets])
        .then(flows => {
            // add decorators
            const existingFlowIds = _.map(flows.data, 'id');
            return serviceBroker
                .loadViewData(CORE_API.LogicalFlowDecoratorStore.findByFlowIdsAndKind, [existingFlowIds])
                .then(decorators => {
                    const decoratorsByFlowId = _.groupBy(decorators.data, 'dataFlowId');
                    const flowsWithDecorators = _.flatMap(flows.data, f => {
                        const decorators = decoratorsByFlowId[f.id];
                        return _.map(decorators, d => Object.assign({}, f, {decorator: d}));
                    });
                    return flowsWithDecorators;
                });
        })
}


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadIdentifierToEntityRefMap = async () => {
        return await serviceBroker
            .loadViewData(CORE_API.ApplicationStore.findAll)
            .then(r => _.keyBy(r.data, k => _.toLower(k.assetCode)));
    };

    const loadCodeToDataTypeMap = async () => {
        return await serviceBroker
            .loadViewData(CORE_API.DataTypeStore.findAll)
            .then(r => _.keyBy(r.data, k => _.toLower(k.code)));
    };

    const isComplete = () => {
        const allParsedRefs = _.flatMap(vm.parsedData, d => [d.source, d.target, d.dataType]);
        return _.every(allParsedRefs, p => p.entityRef != null);
    };

    const parseErrorCount = (data = []) => {
        const allParsedRefs = _.flatMap(data, d => [d.source, d.target, d.dataType]);
        return _.sumBy(allParsedRefs, p => p.entityRef == null ? 1 : 0);
    };


    vm.$onInit = async () => {
        vm.loading = true;
        const [entityRefsByAssetCode, dataTypesByCode] = await $q.all([
            loadIdentifierToEntityRefMap(),
            loadCodeToDataTypeMap()
        ]);

        vm.columnResolvers = {
            source: (identifier) => resolveEntityRef(entityRefsByAssetCode, 'APPLICATION', identifier),
            target: (identifier) => resolveEntityRef(entityRefsByAssetCode, 'APPLICATION', identifier),
            dataType: (identifier) => resolveEntityRef(dataTypesByCode, 'DATA_TYPE', identifier),
        }
    };


    vm.$onChanges = async (changes) => {
        if(vm.columnMappings && vm.sourceData) {
            const mappedData = mapColumns(vm.columnMappings, vm.sourceData, vm.columnResolvers);

            if(mappedData.length > 0 && parseErrorCount(mappedData) == 0) {
                //compare against logical flows in database

                const sourcesAndTargets = _.map(mappedData, p => ({source: p.source.entityRef, target: p.target.entityRef}));
                const existingFlows = await findExistingLogicalFlowsAndDecorators(serviceBroker, sourcesAndTargets);

                // nesting: source -> target -> data type
                const existingFlowsNested = nest()
                    .key(flow => refToString(flow.source))
                    .key(flow => refToString(flow.target))
                    .key(flow => refToString(flow.decorator.decoratorEntity))
                    .object(existingFlows);

                const parsedWithExisting = _.map(mappedData, p => {
                    const sourceRefString = refToString(p.source.entityRef);
                    const targetRefString = refToString(p.target.entityRef);
                    const dataTypeString = refToString(p.dataType.entityRef);
                    const existingFlows = _.get(existingFlowsNested, `[${sourceRefString}][${targetRefString}][]${dataTypeString}`);
                    let existing = null;
                    if(existingFlows && existingFlows.length > 0) {
                        existing = existingFlows[0];
                    }
                    return Object.assign({}, p, {existing});
                });
                vm.parsedData = parsedWithExisting;
            } else {
                vm.parsedData = mappedData;
            }


            vm.loading = false;
            const event = {
                data: vm.parsedData,
                isComplete
            };
            invokeFunction(vm.onParseComplete, event)
        }
    };
}


controller.$inject = [
    '$q',
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzBulkLogicalFlowParser'
};
