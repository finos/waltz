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

import _ from "lodash";
import {isEmpty} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";

import template from './rated-flow-summary-panel.html';


const bindings = {
    entityReference: '<'
};


const initialState = {
    infoCell : null,
    visibility: {
        chart: false
    }
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

    const processSummaries = (xs) => {
        vm.visibility.chart = vm.visibility.chart || _.size(xs) > 0;

        if (vm.entityReference.kind === 'DATA_TYPE') {
            const relevantIds = [];
            const descend = (ptr) => {
                relevantIds.push(ptr);
                _.chain(vm.dataTypes)
                    .filter(c => c.parentId === ptr)
                    .forEach(c => descend(c.id))
                    .value();
            };
            descend(vm.entityReference.id);

            return _.filter(xs, x => {
                const decorator = x.decoratorEntityReference;
                return decorator.kind = 'DATA_TYPE' && _.includes(relevantIds, decorator.id);
            });
        } else {
            return xs;
        }
    };

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => vm.dataTypes = r.data);
    };

    vm.$onChanges = () => {

        if (!vm.entityReference) return;

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
                CORE_API.ApplicationStore.findBySelector,
                [ childSelector ])
            .then(r => vm.apps= r.data);

        serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowDecoratorStore.summarizeInboundBySelector,
                [ childSelector ])
            .then(r => vm.childSummaries = processSummaries(r.data));

        serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowDecoratorStore.summarizeInboundBySelector,
                [ exactSelector ])
            .then(r => vm.exactSummaries = processSummaries(r.data));

    };

    vm.onTableClick = (clickData) => {
        if (clickData.type === 'CELL') {

            const childSelector = {
                entityReference: vm.entityReference,
                scope: 'CHILDREN'
            };

            const flowData = {};
            serviceBroker
                .loadViewData(
                    CORE_API.LogicalFlowStore.findBySelector,
                    [ childSelector ])
                .then(r => {
                    flowData.flows = r.data;
                    return serviceBroker
                        .loadViewData(
                            CORE_API.LogicalFlowDecoratorStore.findBySelector,
                            [ childSelector ]);
                })
                .then(r => {
                    flowData.decorators = r.data;
                    vm.infoPanel = mkInfoCell(clickData, flowData, vm.apps);
                });
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


export default {
    component,
    id: 'waltzRatedFlowSummaryPanel'
};