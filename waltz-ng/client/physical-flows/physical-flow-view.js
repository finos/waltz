import _ from 'lodash';

import {initialiseData} from '../common';
import {green, grey} from '../common/colors';


const template = require('./physical-flow-view.html');


const initialState = {

};



function setupGraphTweakers(application) {
    return {
        node: {
            update: (selection) => {
                selection
                    .select('circle')
                    .attr({
                        'fill': d => d.id === application.id
                            ? green
                            : grey,
                        'stroke': d => d.id === application.id
                            ? green.darker()
                            : grey.darker(),
                        'r': d => d.id === application.id
                            ? 15
                            : 10
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


function mkLineageEntities(lineage = [], specification) {
    return _.chain(lineage)
        .flatMap(
            x => [
                specification.owningEntity,
                x.sourceEntity,
                x.targetEntity])
        .uniqBy('id')
        .value();
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
        .then(ou => vm.organisationalUnit = ou)
        .then(() => vm.graphTweakers = setupGraphTweakers(vm.owningEntity));

    bookmarkStore
        .findByParent(ref)
        .then(bs => vm.bookmarks = bs);

    const lineagePromise = physicalFlowLineageStore
        .findByPhysicalFlowId(flowId)
        .then(lineage => vm.lineage = lineage);

    $q.all([specPromise, lineagePromise])
        .then(() => {
            vm.lineageFlows = mkLineageFlows(vm.lineage);
            vm.lineageEntities = mkLineageEntities(vm.lineage, vm.specification);
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