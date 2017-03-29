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
    parentEntityRef: {
        description: "Change Regional Reporting",
        id: 14,
        kind: "CHANGE_INITIATIVE",
        name: "Change Regional Reporting"
    },
    currentRelationships: [
        {
            entity: { kind: "APPLICATION", id: 1, name: "app 1" },
            relationship: "SUPPORTS"
        },
        {
            entity: { kind: "APPLICATION", id: 2, name: "app 2" },
            relationship: "DEPRECATES"
        },
        {
            entity: { kind: "APPLICATION", id: 3, name: "app 3" },
            relationship: "PARTICIPATES_IN"
        },
    ],
    targetEntityKind: 'APPLICATION',
    allowedRelationships: ['DEPRECATES', 'SUPPORTS', 'PARTICIPATES_IN']
};


function controller() {
    const vm = Object.assign(this, initData);

    vm.onAdd = (entityRef) => {
        console.log(entityRef);
    };

    vm.onRemove = (entityRef) => {
        console.log(entityRef);
    };
}


controller.$inject = [
];


const view = {
    template: require('./playpen1.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;