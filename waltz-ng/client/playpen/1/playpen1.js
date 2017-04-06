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


const initData = {
    entityRef: {
        id: 1,
        name: 'Persian - 0',
        kind: 'APPLICATION'
    }
};


function controller($q, flowDiagramStore, flowDiagramEntityStore) {
    const vm = Object.assign(this, initData);

    const promises = [
        flowDiagramStore.findByEntityReference(vm.entityRef),
        flowDiagramEntityStore.findByEntityReference(vm.entityRef)
    ];

    $q.all(promises)
        .then(([flowDiagrams, flowDiagramEntities]) => {

        vm.flowDiagrams = flowDiagrams;
        vm.flowDiagramEntities = flowDiagramEntities;
    });
}


controller.$inject = [
    '$q',
    'FlowDiagramStore',
    'FlowDiagramEntityStore'
];


const view = {
    template: require('./playpen1.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;