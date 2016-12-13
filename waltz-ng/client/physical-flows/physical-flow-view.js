/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";
import {initialiseData} from "../common";
import {green, grey, blue, actor} from "../common/colors";


const template = require('./physical-flow-view.html');


const initialState = {
    bookmarks: [],
    graph: {
        data: {
            flows: [],
            entities: []
        },
        tweakers: {}
    },
    lineage: [],
    lineageExportFn: () => {},
    mentions: [],
    mentionsExportFn: () => {},
    physicalFlow: null,
    selected: {
        entity: null,
        incoming: [],
        outgoing: []
    },
    specification: null,
    tour: []
};


function mkHistoryObj(flow, spec) {
    return {
        name: spec.name,
        kind: 'PHYSICAL_FLOW',
        state: 'main.physical-flow.view',
        stateParams: { id: flow.id }
    };
}


function addToHistory(historyStore, flow, spec) {
    if (! flow || !spec) { return; }

    const historyObj = mkHistoryObj(flow, spec);

    historyStore.put(
        historyObj.name,
        historyObj.kind,
        historyObj.state,
        historyObj.stateParams);
}


function removeFromHistory(historyStore, flow, spec) {
    if (! flow || !spec) { return; }

    const historyObj = mkHistoryObj(flow, spec);

    historyStore.remove(
        historyObj.name,
        historyObj.kind,
        historyObj.state,
        historyObj.stateParams);
}


function determineFillColor(d, owningEntity, targetEntity) {
    switch (d.id) {
        case (owningEntity.id): return blue;
        case (targetEntity.id):
            return targetEntity.kind === 'APPLICATION'
                ? green
                : actor;
        default: return grey;
    }
}


function determineRadius(d, owningEntity, targetEntity) {
    switch (d.id) {
        case (owningEntity.id): return 8;
        case (targetEntity.id): return 10;
        default: return 6;
    }
}


function determineStrokeColor(d, owningEntity, targetEntity) {
    return determineFillColor(d, owningEntity, targetEntity).darker();
}


function setupGraphTweakers(owningEntity, targetEntity, onClick) {
    return {
        node: {
            enter: (selection) => {
                selection.on('click.view', onClick);
                selection
                    .select('circle')
                    .attrs({
                        'fill': d => determineFillColor(d, owningEntity, targetEntity),
                        'stroke': d => determineStrokeColor(d, owningEntity, targetEntity),
                        'r': d => determineRadius(d, owningEntity, targetEntity)
                    });
            },
            exit: _.identity,
            update: _.identity
        },
        link : {
            update: _.identity,
            enter: _.identity,
            exit: _.identity
        }
    };
}


function mkLineageFlows(lineage = []) {
    return _.map(
        lineage,
        (x) => {
            return {
                id: x.flow.id,
                source: x.sourceEntity,
                target: x.targetEntity
            };
        });
}


function mkLineageEntities(lineage = []) {
    return _.chain(lineage)
        .flatMap(
            x => [
                x.sourceEntity,
                x.targetEntity])
        .uniqBy('id')
        .value();
}


function mkFullLineage(lineage = [], flow, spec) {
    if (!flow || !spec) { return lineage; }
    const finalEntry = {
        flow,
        specification: spec,
        sourceEntity: spec.owningEntity,
        targetEntity: flow.target
    };
    return _.concat(lineage, [finalEntry]);
}


function loadBookmarks(bookmarkStore, entityRef) {
    if(!bookmarkStore || !entityRef) return null;
    return bookmarkStore
        .findByParent(entityRef);
}


function navigateToLastView($state, historyStore) {
    const lastHistoryItem = historyStore.getAll()[0];
    if (lastHistoryItem) {
        $state.go(lastHistoryItem.state, lastHistoryItem.stateParams);
    } else {
        $state.go('main.home');
    }
}


function preparePopover(flowAndSpec) {
    return `
            <div class="small">
                <table class="table small table-condensed">
                    <tr>
                        <th>Format</th>
                        <td><span>${flowAndSpec.specification.format}</span></td>
                    </tr>
                    <tr>
                        <th>Transport</th>
                        <td><span>${flowAndSpec.flow.transport}</span></td>
                    </tr>
                    <tr>
                        <th>Frequency</th>
                        <td><span>${flowAndSpec.flow.frequency}</span></td>
                    </tr>
                    <tr>
                        <th>Basis Offset</th>
                        <td><span>${flowAndSpec.flow.basisOffset}</span></td>
                    </tr>
                </table>
                <div class="text-muted">
                    ${flowAndSpec.description || ""}
                </div>
            </div>
        `;
}


function controller($q,
                    $scope,
                    $state,
                    $stateParams,
                    bookmarkStore,
                    historyStore,
                    notification,
                    physicalFlowLineageStore,
                    physicalSpecificationStore,
                    physicalFlowStore,
                    tourService)
{
    const vm = initialiseData(this, initialState);

    const flowId = $stateParams.id;

    // -- INTERACT ---
    vm.selectEntity = (entity) => {
        const incoming = _.filter(
            vm.lineage,
            f => f.targetEntity.id === entity.id && f.targetEntity.kind === entity.kind);

        const outgoing =  _.filter(
            vm.lineage,
            f => f.sourceEntity.id === entity.id && f.sourceEntity.kind === entity.kind);

        vm.selected = {
            entity,
            incoming,
            outgoing
        };
    };

    vm.preparePopover = preparePopover;

    // -- LOAD ---

    const flowPromise = physicalFlowStore
        .getById(flowId);

    const specPromise = flowPromise
        .then(flow => vm.physicalFlow = flow)
        .then(flow => physicalSpecificationStore.getById(flow.specificationId))
        .then(spec => vm.specification = spec);

    specPromise
        .then(() =>  {
            const specRef = {
                kind: 'PHYSICAL_SPECIFICATION',
                id: vm.specification.id
            };
            return loadBookmarks(bookmarkStore, specRef)
        })
        .then(bs => vm.bookmarks = bs);


    const lineagePromise = physicalFlowLineageStore
        .findByPhysicalFlowId(flowId)
        .then(lineage => vm.lineage = lineage);

    $q.all([specPromise, lineagePromise])
        .then(() => {
            const fullLineage = mkFullLineage(vm.lineage, vm.physicalFlow, vm.specification);

            vm.graph = {
                data: {
                    entities: mkLineageEntities(fullLineage),
                    flows: mkLineageFlows(fullLineage)
                },
                tweakers: setupGraphTweakers(
                    vm.specification.owningEntity,
                    vm.physicalFlow.target,
                    (d) => $scope.$applyAsync(() => vm.selectEntity(d)))
            };
        })
        .then(() => tourService.initialiseForKey('main.physical-flow.view', true))
        .then(tour => vm.tour = tour)
        .then(() => addToHistory(historyStore, vm.physicalFlow, vm.specification));

    physicalFlowLineageStore
        .findContributionsByPhysicalFlowId(flowId)
        .then(mentions => vm.mentions = mentions);

    vm.onLineagePanelInitialise = (e) => {
        vm.lineageExportFn = e.exportFn;
    };

    vm.exportLineage = () => {
        vm.lineageExportFn();
    };

    vm.onMentionsPanelInitialise = (e) => {
        vm.mentionsExportFn = e.exportFn;
    };

    vm.exportMentions = () => {
        vm.mentionsExportFn();
    };

    const deleteSpecification = () => {
        physicalSpecificationStore.deleteById(vm.specification.id)
            .then(r => {
                if (r.outcome === 'SUCCESS') {
                    notification.success(`Specification ${vm.specification.name} deleted`);
                } else {
                    notification.error(r.message);
                }
                navigateToLastView($state, historyStore);
            })
    };

    const handleDeleteFlowResponse = (response) => {
        if (response.outcome === 'SUCCESS') {
            notification.success('Physical flow deleted');
            removeFromHistory(historyStore, vm.physicalFlow, vm.specification);

            const deleteSpecText = `The specification ${vm.specification.name} is no longer referenced by any physical flow. Do you want to delete the specification?`;
            if (response.isSpecificationUnused && confirm(deleteSpecText)) {
                deleteSpecification();
            } else {
                navigateToLastView($state, historyStore);
            }
        } else {
            notification.error(response.message);
        }
    };

    vm.deleteFlow = () => {
        if (confirm('Are you sure you want to delete this flow ?')) {
            physicalFlowStore
                .deleteById(flowId)
                .then(r => handleDeleteFlowResponse(r));
        }
    };
}


controller.$inject = [
    '$q',
    '$scope',
    '$state',
    '$stateParams',
    'BookmarkStore',
    'HistoryStore',
    'Notification',
    'PhysicalFlowLineageStore',
    'PhysicalSpecificationStore',
    'PhysicalFlowStore',
    'TourService'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};
