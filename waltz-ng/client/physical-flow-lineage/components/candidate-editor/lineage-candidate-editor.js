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
import {initialiseData} from "../../../common";
import {enrichConsumes} from "../../../physical-specifications/utilities";


const bindings = {
    currentLineage: '<',
    logicalFlows: '<',
    physicalFlows: '<',
    specifications: '<',
    onRefocus: '<',
    onAdd: '<',
    onRemove: '<',
    contributingFlowIds: '<'
};


const template = require('./lineage-candidate-editor.html');

const initialState = {
    selectedSource: null,
    candidateSelections: []
}


function removeCandidatesWithNoPhysicalFlows(candidates = []) {
    return _.filter(candidates, c => c.physicalFlow != null);
}


function removeUsedCandidates(candidates = [], currentLineage = []) {
    const usedFlowIds = _.map(currentLineage, 'flow.id');

    return _.reject(candidates, c => _.includes(usedFlowIds, c.physicalFlow.id));
}


function mkCandidateGroups(flowData = [], currentLineage = []) {
    const candidates = removeCandidatesWithNoPhysicalFlows(flowData);
    const validCandidates = removeUsedCandidates(candidates, currentLineage);

    const groupedBySource = _.groupBy(validCandidates, c => c.sourceRef.kind + '_' + c.sourceRef.id);
    const sourcesAndCounts = _.chain(validCandidates)
        .map(c => c.sourceRef)
        .keyBy(c => c.kind + '_' + c.id)
        .map((v, k) => ({source: v, count: groupedBySource[k].length}))
        .value();

    return {
        sources: sourcesAndCounts,
        candidatesBySource: groupedBySource,
    };
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        const specOwners = _.map(vm.specifications, "owningEntity");
        const flowTargets = _.map(vm.physicalFlows, "target");

        const endpointReferences = _.chain(_.concat(specOwners, flowTargets))
            .uniqBy("id")
            .value();


        const flowData = enrichConsumes(
            vm.specifications,
            vm.physicalFlows,
            endpointReferences);

        const candidateGroups = mkCandidateGroups(flowData, vm.currentLineage);
        vm.sources = candidateGroups.sources;
        vm.candidatesBySource = candidateGroups.candidatesBySource;

        mkCandidateSelections(vm.selectedSource);
    };


    const mkCandidateSelections = (entityRef) => {
        vm.candidateSelections = entityRef
            ? vm.candidatesBySource[entityRef.kind + "_" + entityRef.id]
            : [];
    };


    vm.selectSource = (entityRef) => {
        vm.selectedSource = entityRef;
        mkCandidateSelections(entityRef);
    }
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller,
    transclude: {
        'empty': 'empty'
    }
};

export default component;