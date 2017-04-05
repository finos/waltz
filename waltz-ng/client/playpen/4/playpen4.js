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


const initialState = {
    physicalFlows: [],
    specifications: [],
    onInitialise: () => console.log('init'),
    onChange: () => console.log('change')
};


function controller($q,
                    $stateParams,
                    physicalSpecificationStore,
                    physicalFlowStore,
                    logicalFlowStore) {

    const vm = Object.assign(this, initialState);

    const entityReference = {
        id: 1,
        kind: 'FLOW_DIAGRAM'
    };

    const idSelector = {
        entityReference,
        scope: 'EXACT'
    };

    physicalSpecificationStore
        .findByEntityReference(entityReference)
        .then(x => {
            console.log('specs', x);
            return x;
        })
        .then(specs => vm.specifications = specs);

    physicalFlowStore
        .findByEntityReference(entityReference)
        .then(x => {
            console.log('pfs', x);
            return x;
        })
        .then(pfs => vm.physicalFlows = pfs);

    logicalFlowStore
        .findByEntityReference(entityReference)
        .then(x => {
            console.log('lfs', x);
            return x;
        })
        .then(xs => vm.logicalFlows = xs);

    vm.entityRef = entityReference;
}


controller.$inject = [
    '$q',
    '$stateParams',
    'PhysicalSpecificationStore',
    'PhysicalFlowStore',
    'LogicalFlowStore',
    'FlowDiagramAnnotationStore',
    'FlowDiagramEntityStore',
    'FlowDiagramStore'
];


const view = {
    template: require('./playpen4.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;
