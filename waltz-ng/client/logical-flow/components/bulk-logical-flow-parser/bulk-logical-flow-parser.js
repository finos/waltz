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

    onInitialise: '<',
    onParseComplete: '<'
};


const initialState = {
    columnMappings: {},
    filterCriteria: null,
    filteredData: [],
    loading: false,
    parsedData: [],
    sourceData: [],
    summary: {},

    onInitialise: (event) => console.log('default onInitialise handler for bulk-logical-flow-parser: ', event),
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


function resolveEntities(columnMappings = {}, sourceData = [], columnResolvers = {}) {
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
            [sourcesAndTargets],
            { force: true })
        .then(flows => {
            // add decorators
            const existingFlowIds = _.map(flows.data, 'id');
            return serviceBroker
                .loadViewData(CORE_API.LogicalFlowDecoratorStore.findByFlowIdsAndKind, [existingFlowIds], { force: true })
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


function mkParseSummary(data = []) {
    const allParsedRefs = _.flatMap(data, d => [d.source, d.target, d.dataType]);
    return Object.assign(
        {
            total: data.length,
            newFlows: 0,
            existingFlows: 0,
            missingEntities: 0,
            foundEntities: 0
        },
        _.countBy(data, r => r.existing == null ? 'newFlows' : 'existingFlows'),
        _.countBy(allParsedRefs, p => p.entityRef == null ? 'missingEntities' : 'foundEntities'));
}


function mkFilterPredicate(criteria) {
    switch (criteria) {
        case 'NOT_FOUND':
            return (r) => r.source.entityRef === null
                || r.target.entityRef === null
                || r.dataType.entityRef === null;
        case 'NEW':
            return (r) => r.existing === null;
        case 'EXISTING':
            return (r) => r.existing !== null;
        default:
            return (r) => true;
    }
}


function controller($q, $scope, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadIdentifierToEntityRefMap = () => {
        return serviceBroker
            .loadViewData(CORE_API.ApplicationStore.findAll)
            .then(r => _.keyBy(r.data, k => _.toLower(k.assetCode)));
    };

    const loadCodeToDataTypeMap = () => {
        return serviceBroker
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

    const filterResults = (criteria) => {
        vm.filterCriteria = criteria;
        return _.filter(vm.parsedData, mkFilterPredicate(criteria));
    };

    const resolveFlows = async () => {
        if(vm.columnMappings && vm.sourceData) {
            const resolvedData = resolveEntities(vm.columnMappings, vm.sourceData, vm.columnResolvers);

            if(resolvedData.length === 0 || parseErrorCount(resolvedData) > 0) {
                return resolvedData;
            }

            //compare against logical flows in database
            const sourcesAndTargets = _.map(resolvedData, p => ({source: p.source.entityRef, target: p.target.entityRef}));
            const existingFlows = await findExistingLogicalFlowsAndDecorators(serviceBroker, sourcesAndTargets);

            // nesting: source -> target -> data type
            const existingFlowsBySourceByTargetByDataType = nest()
                .key(flow => refToString(flow.source))
                .key(flow => refToString(flow.target))
                .key(flow => refToString(flow.decorator.decoratorEntity))
                .object(existingFlows);

            const resolvedFlowsWithExisting = _
                .chain(resolvedData)
                .map(p => {
                    const sourceRefString = refToString(p.source.entityRef);
                    const targetRefString = refToString(p.target.entityRef);
                    const dataTypeString = refToString(p.dataType.entityRef);
                    const existingFlows = _.get(existingFlowsBySourceByTargetByDataType, `[${sourceRefString}][${targetRefString}][${dataTypeString}]`);
                    let existing = null;
                    if(existingFlows && existingFlows.length > 0) {
                        existing = existingFlows[0];
                    }
                    return Object.assign({}, p, {existing});
                })
                .sortBy([
                    o => o.source.entityRef.name,
                    o => o.target.entityRef.name,
                    o => o.dataType.entityRef.name,
                ])
                .value();
            return resolvedFlowsWithExisting;
        }
    };


    const parseFlows = () => {
        vm.loading = true;
        $q.all([
            loadIdentifierToEntityRefMap(),
            loadCodeToDataTypeMap()
        ]).then(([entityRefsByAssetCode, dataTypesByCode]) => {
            vm.columnResolvers = {
                'source': (identifier) => resolveEntityRef(entityRefsByAssetCode, 'APPLICATION', identifier),
                'target': (identifier) => resolveEntityRef(entityRefsByAssetCode, 'APPLICATION', identifier),
                'dataType': (identifier) => resolveEntityRef(dataTypesByCode, 'DATA_TYPE', identifier),
            };
            return resolveFlows()
                .then(flows => {
                    vm.parsedData = flows;
                    vm.filteredData = filterResults();
                    vm.summary = mkParseSummary(vm.parsedData);

                    $scope.$apply(() => vm.loading = false);

                    const event = {
                        data: vm.parsedData,
                        isComplete
                    };
                    invokeFunction(vm.onParseComplete, event);
                });
        });
    };


    vm.$onInit = () => {
        const api = {
            parseFlows
        };
        invokeFunction(vm.onInitialise, api);
    };


    vm.$onChanges =(changes) => {
    };


    vm.applyFilter = (criteria) => {
        vm.filteredData = filterResults(criteria);
    };
}


controller.$inject = [
    '$q',
    '$scope',
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
