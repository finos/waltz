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
import _ from 'lodash';
import {initialiseData} from '../common';

const template = require('./flow-diagram-view.html');

const initialState = {
    visibility: {}
};


function controller(
    $q,
    $stateParams,
    $timeout,
    flowDiagramStateService,
    flowDiagramStore,
    flowDiagramEntityStore,
    logicalFlowStore,
    physicalFlowStore,
    physicalSpecificationStore)
{
    const vm = initialiseData(this, initialState);
    const diagramId = $stateParams.id;

    const loadVisibility = () =>
        vm.visibility.layers = flowDiagramStateService.getState().visibility.layers;

    flowDiagramStateService
        .load(diagramId)
        .then(loadVisibility);

    flowDiagramStore
        .getById(diagramId)
        .then(d => vm.diagram = d);

    flowDiagramEntityStore
        .findByDiagramId(diagramId)
        .then(xs => {
            vm.nodes = _
                .chain(xs)
                .map('entityReference')
                .filter(x => x.kind === 'APPLICATION' || x.kind === 'ACTOR')
                .sortBy('name')
                .value();
        });

    const selector = {
        entityReference: { id: diagramId, kind: 'FLOW_DIAGRAM' },
        scope: 'EXACT'
    };

    const physicalFlowPromise = physicalFlowStore
        .findBySelector(selector);

    const physicalSpecPromise = physicalSpecificationStore
        .findBySelector(selector)
        .then(xs => {
            return xs;
        });

    const logicalFlowPromise = logicalFlowStore
        .findBySelector(selector);

    $q.all([logicalFlowPromise, physicalFlowPromise, physicalSpecPromise])
        .then(([logicalFlows, physicalFlows, physicalSpecs]) => {
            const physicalFlowsByLogicalId = _.groupBy(physicalFlows, 'logicalFlowId');
            vm.physicalSpecificationsById = _.keyBy(physicalSpecs, 'id');
            vm.physicalFlows = physicalFlows;
            vm.flows = _.map(logicalFlows, f => {
                return {
                    logicalFlow: f,
                    physicalFlows: physicalFlowsByLogicalId[f.id] || [],
                }
            });
        });


    vm.clickHandlers =  {
        node: d => $timeout(
            () => vm.highlightIds = [d.data.id],
            0),
        flowBucket: d => $timeout(
            () => vm.highlightIds = [d.data.id],
            0)
    };

    vm.toggleLayer = (layer) => {
        const currentlyVisible = flowDiagramStateService.getState().visibility.layers[layer];
        const cmd = {
            command: currentlyVisible ? 'HIDE_LAYER' : 'SHOW_LAYER',
            payload: layer
        };
        flowDiagramStateService.processCommands([cmd]);
        loadVisibility();
    }

}

controller.$inject = [
    '$q',
    '$stateParams',
    '$timeout',
    'FlowDiagramStateService',
    'FlowDiagramStore',
    'FlowDiagramEntityStore',
    'LogicalFlowStore',
    'PhysicalFlowStore',
    'PhysicalSpecificationStore'
];

const view = {
    template,
    controller,
    controllerAs: 'ctrl'
};

export default view;