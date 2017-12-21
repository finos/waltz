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
import { CORE_API } from '../../../common/services/core-api-utils';
import { initialiseData } from '../../../common';
import { toEntityRef } from '../../../common/entity-utils';


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


function resolveEntityRef(entitiesByIdentifier = {}, identifier) {
    const search = _.toLower(identifier);
    const app = entitiesByIdentifier[search];
    const entityRef = app ? toEntityRef(app, 'APPLICATION') : null;

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

    vm.$onInit = async () => {
        vm.loading = true;
        const [entityRefsByAssetCode, dataTypesByCode] = await $q.all([
            loadIdentifierToEntityRefMap(),
            loadCodeToDataTypeMap()
        ]);

        vm.columnResolvers = {
            source: (identifier) => resolveEntityRef(entityRefsByAssetCode, identifier),
            target: (identifier) => resolveEntityRef(entityRefsByAssetCode, identifier),
            dataType: (identifier) => resolveEntityRef(dataTypesByCode, identifier),
        }
    };


    vm.$onChanges = async (changes) => {
        if(vm.columnMappings && vm.sourceData) {
            vm.parsedData = mapColumns(vm.columnMappings, vm.sourceData, vm.columnResolvers);
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
