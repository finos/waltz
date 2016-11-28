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