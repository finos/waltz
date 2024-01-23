/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import _ from "lodash";
import {nest} from "d3-collection";
import {CORE_API} from "../../../common/services/core-api-utils";
import {initialiseData} from "../../../common";
import {refToString, sameRef, toEntityRefWithKind} from "../../../common/entity-utils";
import {invokeFunction} from "../../../common/index";
import {downloadTextFile} from "../../../common/file-utils";


import template from "./bulk-logical-flow-parser.html";


const bindings = {
    columnMappings: '<',
    sourceData: '<',

    onInitialise: '<',
    onParseComplete: '<'
};


const initialState = {
    columnDefs: [],
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
    const search = _.chain(identifier)
        .toLower()
        .trim()
        .value();
    const app = entitiesByIdentifier[search];
    const entityRef = app ? toEntityRefWithKind(app, kind) : null;

    return {
        identifier,
        entityRef
    };
}


function mkEntityLinkColumnDef(columnHeading, entityRefField, identifierField) {
    return {
        field: entityRefField + '.name',
        displayName: columnHeading,
        cellTemplate: `
            <div class="ui-grid-cell-contents">
                <waltz-entity-link ng-if="row.entity.${entityRefField}"
                                   entity-ref="row.entity.${entityRefField}">
                </waltz-entity-link>
                <span ng-if="!row.entity.${entityRefField}"
                      class="text-danger bg-danger">
                    <strong ng-bind="row.entity.${identifierField}"></strong>
                    <small>not found</small>
                </span>
            </div>`
    };
}


function mkColumnDefs() {
    return [
        mkEntityLinkColumnDef('Source', 'source.entityRef', 'source.identifier'),
        mkEntityLinkColumnDef('Target', 'target.entityRef', 'target.identifier'),
        mkEntityLinkColumnDef('Data Type', 'dataType.entityRef', 'dataType.identifier'),
        {
            field: 'existing',
            displayName: '',
            width: '10%',
            cellTemplate: `
                <div class="ui-grid-cell-contents">
                    <span ng-if="COL_FIELD === null"
                          class="label label-success">New</span>
                    <span ng-if="COL_FIELD"
                          class="label label-warning">Exists</span>
                </div>`
        }
    ];
}


function resolveEntities(columnMappings = {}, sourceData = [], columnResolvers = {}) {
    const targetKeys = _.keys(columnMappings);
    const mappedObjects = _.map(sourceData, sourceObj => {
        const targetObj = {};

        _.forEach(targetKeys, targetColumn => {
            const sourceColumn = columnMappings[targetColumn];
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
                .loadViewData(CORE_API.DataTypeDecoratorStore.findByFlowIds, [existingFlowIds], { force: true })
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
    const summary = Object.assign(
        {
            total: data.length,
            newFlows: 0,
            existingFlows: 0,
            missingEntities: 0,
            foundEntities: 0,
            circularFlows: 0,
            nonCircularFlows: data.length
        },
        _.countBy(data, r => r.existing == null ? 'newFlows' : 'existingFlows'),
        _.countBy(allParsedRefs, p => p.entityRef == null ? 'missingEntities' : 'foundEntities'),
        _.countBy(data, r => r.source.entityRef && r.target.entityRef && sameRef(r.source.entityRef, r.target.entityRef) ? 'circularFlows' : 'nonCircularFlows'));

    summary.errors = summary.missingEntities + summary.circularFlows;
    return summary;
}


function mkFilterPredicate(criteria) {
    switch (criteria) {
        case 'ERROR':
            return (r) => r.source.entityRef === null
                || r.target.entityRef === null
                || r.dataType.entityRef === null
                || r.source.entityRef && r.target.entityRef && sameRef(r.source.entityRef, r.target.entityRef);
        case 'NEW':
            return (r) => r.existing === null;
        case 'EXISTING':
            return (r) => r.existing !== null;
        default:
            return (r) => true;
    }
}


function parseErrorCount(data = []) {
    const allParsedRefs = _.flatMap(data, d => [d.source, d.target, d.dataType]);
    const circularFlowCount = _.sumBy(data, r => r.source.entityRef
                                        && r.target.entityRef
                                        && sameRef(r.source.entityRef, r.target.entityRef) ? 1 : 0);
    const missingEntityRefs =  _.sumBy(allParsedRefs, p => p.entityRef == null ? 1 : 0);

    return missingEntityRefs + circularFlowCount;
}


function controller($q, $scope, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.columnDefs = mkColumnDefs();

    const loadIdentifierToEntityRefMap = () => {
        return serviceBroker
            .loadViewData(CORE_API.ApplicationStore.findAll)
            .then(r => _.keyBy(r.data, k => _.toLower(k.assetCode)));
    };

    const loadDataTypeMap = () => {
        return serviceBroker
            .loadViewData(CORE_API.DataTypeStore.findAll)
            .then(r => {
                const dataTypesByCode = _.keyBy(r.data, k => _.toLower(k.code));
                const dataTypesByName = _.keyBy(r.data, k => _.toLower(k.name));
                return Object.assign({}, dataTypesByCode, dataTypesByName);
            });
    };

    const isComplete = () => {
        return parseErrorCount(vm.parsedData) === 0;
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
            loadDataTypeMap()
        ]).then(([entityRefsByAssetCode, dataTypeMap]) => {
            vm.columnResolvers = {
                'source': (identifier) => resolveEntityRef(entityRefsByAssetCode, 'APPLICATION', identifier),
                'target': (identifier) => resolveEntityRef(entityRefsByAssetCode, 'APPLICATION', identifier),
                'dataType': (identifier) => resolveEntityRef(dataTypeMap, 'DATA_TYPE', identifier),
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
                })
                .catch(err => console.err('error resolving flows: ', err));
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


    vm.exportParseErrors = () => {

        const header = [
            "Source",
            "Target",
            "Data Type"
        ];

        const dataRows = _
            .chain(vm.filteredData)
            .filter(mkFilterPredicate('ERROR'))
            .map(flow => {
                return [
                    _.get(flow.source, 'entityRef.name', flow.source.identifier + ' not found'),
                    _.get(flow.target, 'entityRef.name', flow.target.identifier + ' not found'),
                    _.get(flow.dataType, 'entityRef.name', flow.dataType.identifier + ' not found')
                ];
            })
            .value();

        const rows = [header]
            .concat(dataRows);

        downloadTextFile(rows, ",", `logical_flow_errors.csv`);
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
