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

import {initialiseData} from "../common";
import _ from "lodash";
import {green, grey, blue, actor} from "../common/colors";


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
    return determineFillColor(d, owningEntity, targetEntity)
        .darker();
}


function setupGraphTweakers(owningEntity, targetEntity, onClick) {
    return {
        node: {
            update: _.identity,
            enter: (selection) => {
                selection
                    .on('click.edit', onClick);

                selection
                    .selectAll('circle')
                    .attr('fill', d => determineFillColor(d, owningEntity, targetEntity))
                    .attr('stroke', d => determineStrokeColor(d, owningEntity, targetEntity))
                    .attr('r', d => determineRadius(d, owningEntity, targetEntity));

                selection
                    .selectAll('text')
                    .style('fill', d => determineStrokeColor(d, owningEntity, targetEntity));
            },
            exit: _.identity
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


const template = require('./lineage-edit.html');


const initialState = {
    contributors : [],
    searchResults: {
        entityRef: null,
        physicalFlows: [],
        specifications: [],
        loading: false
    },
    visibility: {
        report: {
            nameEditor: false,
            descriptionEditor: false
        },
    },
    graph: {
        data: {
            flows: [],
            entities: []
        },
        tweakers: {}
    },
};


function mergeEntities(originals = [], updates = []) {
    const originalsById = _.keyBy(originals, 'id');

    return _.map(updates, u => {
        const existing = originalsById[u.id];
        return existing ? existing : u;
    });
}


function controller($q,
                    $scope,
                    $stateParams,
                    physicalFlowLineageStore,
                    notification,
                    physicalSpecificationStore,
                    physicalFlowStore) {

    const vm = initialiseData(this, initialState);

    const physicalFlowId = $stateParams.id;

    const loadLineage = () => physicalFlowLineageStore
        .findByPhysicalFlowId(physicalFlowId)
        .then(lineage => vm.lineage = lineage)
        .then(() => {
            const fullLineage = mkFullLineage(vm.lineage, vm.describedFlow, vm.describedSpecification);
            const graphEntities = mkLineageEntities(fullLineage);
            const graphFlows = mkLineageFlows(fullLineage);
            const nodeClickHandler = (d) => $scope.$applyAsync(() => {
                if(vm.describedFlow.target === d) {
                    resetSearch();
                    vm.searchResults.entityRef = d;
                } else {
                    vm.doSearch(d);
                }
            });

            vm.graph = {
                data: {
                    flows: graphFlows,
                    entities: mergeEntities(vm.graph.data.entities, graphEntities),
                },
                tweakers: setupGraphTweakers(
                    vm.describedSpecification.owningEntity,
                    vm.describedFlow.target,
                    nodeClickHandler)
            }
        });


    physicalFlowStore.getById(physicalFlowId)
        .then(flow => vm.describedFlow = flow)
        .then(flow => physicalSpecificationStore.getById(flow.specificationId))
        .then(spec => {
            vm.describedSpecification = spec;
            loadLineage();
            vm.doSearch(vm.describedSpecification.owningEntity);
        });


    function resetSearch() {
        vm.searchResults.loading = false;
        vm.searchResults.physicalFlows = [];
        vm.searchResults.specifications = [];
    }


    function searchForCandidateSpecifications( ref ) {
        resetSearch();

        vm.searchResults.loading = true;
        vm.searchResults.entityRef = ref;

        const promises = [
            physicalSpecificationStore
                .findByConsumerEntityReference(ref)
                .then(xs => vm.searchResults.specifications = xs),
            physicalFlowStore
                .findByConsumerEntityReference(ref)
                .then(xs => vm.searchResults.physicalFlows = xs)
        ];

        $q.all(promises)
            .then(() => vm.searchResults.loading = false);
    }

    // -- INTERACTION

    vm.doSearch = (ref) => searchForCandidateSpecifications(ref);


    vm.addPhysicalFlow = (physicalFlowId) => {
        physicalFlowLineageStore
            .addContribution(vm.describedFlow.id, physicalFlowId)
            .then(() => notification.success("added"))
            .then(() => loadLineage());
    };

    vm.removePhysicalFlow = (physicalFlow) => {
        physicalFlowLineageStore
            .removeContribution(vm.describedFlow.id, physicalFlow.id)
            .then(() => notification.warning("removed"))
            .then(() => loadLineage());
    };

}


controller.$inject = [
    '$q',
    '$scope',
    '$stateParams',
    'PhysicalFlowLineageStore',
    'Notification',
    'PhysicalSpecificationStore',
    'PhysicalFlowStore'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};