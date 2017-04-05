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

import {initialiseData} from "../common";
import _ from "lodash";


const template = require('./physical-flow-view.html');


const initialState = {
    bookmarks: [],
    mentions: [],
    mentionsExportFn: () => {},
    physicalFlow: null,
    selected: {
        entity: null,
        incoming: [],
        outgoing: []
    },
    specification: null,
    selectedSpecDefinition: {},
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


function getSelectedSpecDefinition(specDefinitions = [], selectedSpecDefId = null) {
    if (selectedSpecDefId) {
        const defsById = _.keyBy(specDefinitions, 'id');
        return defsById[selectedSpecDefId];
    } else {
        // find the active definition
        return _.find(specDefinitions, d => d.status === 'ACTIVE');
    }
}


function controller($q,
                    $state,
                    $stateParams,
                    bookmarkStore,
                    historyStore,
                    logicalFlowStore,
                    notification,
                    physicalFlowStore,
                    physicalSpecDefinitionStore,
                    physicalSpecDefinitionFieldStore,
                    physicalSpecDefinitionSampleFileStore,
                    physicalSpecificationStore,
                    tourService)
{
    const vm = initialiseData(this, initialState);

    const flowId = $stateParams.id;

    // -- LOAD ---

    const physicalFlowPromise = physicalFlowStore
        .getById(flowId)
        .then(flow => vm.physicalFlow = flow);

    physicalFlowPromise
        .then(physicalFlow => logicalFlowStore.getById(physicalFlow.logicalFlowId))
        .then(logicalFlow => vm.logicalFlow = logicalFlow);

    const specPromise = physicalFlowPromise
        .then(physicalFlow => physicalSpecificationStore.getById(physicalFlow.specificationId))
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

    // spec definitions
    specPromise
        .then(() => physicalSpecDefinitionStore.findForSpecificationId(vm.physicalFlow.specificationId))
        .then(specDefs => vm.selectedSpecDefinition.def
                        = getSelectedSpecDefinition(specDefs, vm.physicalFlow.specificationDefinitionId))
        .then(() => {
            if (vm.selectedSpecDefinition.def) {
                const specDefFieldPromise = physicalSpecDefinitionFieldStore
                    .findForSpecDefinitionId(vm.selectedSpecDefinition.def.id);

                const specDefSampleFilePromise = physicalSpecDefinitionSampleFileStore
                    .findForSpecDefinitionId(vm.selectedSpecDefinition.def.id);

                $q.all([specDefFieldPromise, specDefSampleFilePromise])
                    .then(([fields, file]) => {
                        vm.selectedSpecDefinition.fields = fields;
                        vm.selectedSpecDefinition.sampleFile = file;
                    });
            }
        });

    // tour
    specPromise
        .then(() => tourService.initialiseForKey('main.physical-flow.view', true))
        .then(tour => vm.tour = tour)
        .then(() => addToHistory(historyStore, vm.physicalFlow, vm.specification));


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
    '$state',
    '$stateParams',
    'BookmarkStore',
    'HistoryStore',
    'LogicalFlowStore',
    'Notification',
    'PhysicalFlowStore',
    'PhysicalSpecDefinitionStore',
    'PhysicalSpecDefinitionFieldStore',
    'PhysicalSpecDefinitionSampleFileStore',
    'PhysicalSpecificationStore',
    'TourService'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};
