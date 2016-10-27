import _ from 'lodash';

import {initialiseData} from '../common';
import {green, grey, blue, actor} from '../common/colors';


const template = require('./physical-flow-view.html');


const initialState = {

};

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
    switch (d.id) {
        case (owningEntity.id): return blue.darker();
        case (targetEntity.id):
            return targetEntity.kind === 'APPLICATION'
                ? green.darker()
                : actor.darker();
        default: return grey;
    }
}


function setupGraphTweakers(owningEntity, targetEntity) {
    return {
        node: {
            update: (selection) => {
                selection
                    .select('circle')
                    .attr({
                        'fill': d => determineFillColor(d, owningEntity, targetEntity),
                        'stroke': d => determineStrokeColor(d, owningEntity, targetEntity),
                        'r': d => determineRadius(d, owningEntity, targetEntity)
                    });
            },
            exit: () => {},
            enter: () => {}
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


function controller($q,
                    $stateParams,
                    applicationStore,
                    bookmarkStore,
                    orgUnitStore,
                    physicalFlowLineageStore,
                    physicalSpecificationStore,
                    physicalFlowStore)
{
    const vm = initialiseData(this, initialState);

    const flowId = $stateParams.id;
    const ref = {
        kind: 'PHYSICAL_FLOW',
        id: flowId
    };

    // -- LOAD ---

    const flowPromise = physicalFlowStore
        .getById(flowId);

    const specPromise = flowPromise
        .then(flow => vm.physicalFlow = flow)
        .then(flow => physicalSpecificationStore.getById(flow.specificationId))
        .then(spec => vm.specification = spec);

    specPromise
        .then(spec => applicationStore.getById(spec.owningEntity.id))
        .then(app => vm.owningEntity = app)
        .then(app => orgUnitStore.getById(app.organisationalUnitId))
        .then(ou => vm.organisationalUnit = ou);

    bookmarkStore
        .findByParent(ref)
        .then(bs => vm.bookmarks = bs);

    const lineagePromise = physicalFlowLineageStore
        .findByPhysicalFlowId(flowId)
        .then(lineage => vm.lineage = lineage);

    $q.all([specPromise, lineagePromise])
        .then(() => {
            vm.graphTweakers = setupGraphTweakers(vm.specification.owningEntity, vm.physicalFlow.target);

            const fullLineage = mkFullLineage(vm.lineage, vm.physicalFlow, vm.specification);
            vm.lineageFlows = mkLineageFlows(fullLineage);
            vm.lineageEntities = mkLineageEntities(fullLineage);
        });

    physicalFlowLineageStore
        .findContributionsByPhysicalFlowId(flowId)
        .then(mentions => vm.mentions = mentions);

}


controller.$inject = [
    '$q',
    '$stateParams',
    'ApplicationStore',
    'BookmarkStore',
    'OrgUnitStore',
    'PhysicalFlowLineageStore',
    'PhysicalSpecificationStore',
    'PhysicalFlowStore'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};