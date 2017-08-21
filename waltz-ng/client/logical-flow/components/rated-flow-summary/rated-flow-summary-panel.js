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

import _ from "lodash";
import {isEmpty} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";


const bindings = {
    apps: '<',
    entityReference: '<',
    flowData: '<',
    onLoadDetail: '<'
};


const template = require('./rated-flow-summary-panel.html');


const initialState = {
    apps: [],
    infoCell : null
};


function findMatchingDecorators(selection, decorators = []) {
    const matcher = {
        decoratorEntity: {
            kind: 'DATA_TYPE',
            id: selection.dataType.id
        },
        rating: selection.rating
    };

    return _.filter(decorators, matcher);
}


function findMatchingFlows(flows = [], decorators = []) {
    const flowIds = _.chain(decorators)
        .map('dataFlowId')
        .uniq()
        .value();

    return _.filter(
        flows,
        f => _.includes(flowIds, f.id));
}


function findConsumingApps(flows = [], apps = []) {
    const appsById = _.keyBy(apps, 'id');
    return _.chain(flows)
        .filter(f => f.target.kind === 'APPLICATION')
        .map('target.id')
        .uniq()
        .map(id => appsById[id])
        .value();
}


/**
 * Only interested in flows coming into this group
 * @param flows
 * @param apps
 * @returns {Array}
 */
function findPotentialFlows(flows = [], apps =[]) {
    const appIds = _.map(apps, 'id');
    return _.filter(flows, f => _.includes(appIds, f.target.id));
}


function mkInfoCell(selection, flowData, apps = []) {
    if (! selection) return null;
    if (isEmpty(flowData.flows)) return null;

    const potentialFlows = findPotentialFlows(flowData.flows, apps);

    const matchingDecorators = findMatchingDecorators(selection, flowData.decorators);
    const matchingFlows = findMatchingFlows(potentialFlows, matchingDecorators )
    const consumingApps = findConsumingApps(matchingFlows, apps);

    return { dataType: selection.dataType, rating: selection.rating, applications: consumingApps };
}


function controller(serviceBroker)
{
    const vm = _.defaultsDeep(this, initialState);

    const childSelector = {
        entityReference: vm.entityReference,
        scope: 'CHILDREN'
    };

    const exactSelector = {
        entityReference: vm.entityReference,
        scope: 'EXACT'
    };



    serviceBroker
        .loadViewData(
            CORE_API.LogicalFlowDecoratorStore.summarizeBySelector,
            [ childSelector ])
        .then(r => vm.childSummaries = r.data);

    serviceBroker
        .loadViewData(
            CORE_API.LogicalFlowDecoratorStore.summarizeBySelector,
            [ exactSelector ])
        .then(r => vm.exactSummaries = r.data);

    serviceBroker
        .loadAppData(CORE_API.DataTypeStore.findAll)
        .then(r => vm.dataTypes = r.data);

    vm.onTableClick = (clickData) => {
        if (clickData.type === 'CELL') {
            vm.onLoadDetail()
                .then(() => vm.infoPanel = mkInfoCell(clickData, vm.flowData, vm.apps));
        } else {
            console.log('rated-flow-summary-panel: unsupported selection', clickData);
        }
    };

}


controller.$inject = [
    'ServiceBroker'
];


const component = {
    template,
    controller,
    bindings
};


export default component;