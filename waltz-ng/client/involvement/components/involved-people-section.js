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
import {initialiseData} from "../../common";

import template from './involved-people-section.html';


const bindings = {
    entityRef: '<',
    involvements: '<',
    involvementKinds: '<',

    onAdd: '<',
    onRemove: '<'
};


const initialState = {
    allowedInvolvements: [],
    currentInvolvements: [],
    involvements: [],
    gridData: [],
    gridDataCount: 0,
    exportGrid: () => {},
    visibility: {
        editor: false
    },
    onAdd: () => console.log("default onAdd handler for involved-people-section"),
    onRemove: () => console.log("default onRemove handler for involved-people-section")
};


function mkGridData(involvements = [], displayNameService) {
    return _.chain(involvements)
        .map(inv => {
            const roles = _.join(
                _.map(inv.involvements, ik => displayNameService.lookup('involvementKind', ik.kindId)),
                ', '
            );

            return {
                person: inv.person,
                roles: roles
            }
        })
        .value();
}


function mkEntityRef(person) {
    if (person) {
        return {
            id: person.id,
            name: person.displayName,
            kind: 'PERSON'
        };
    }
    return person;
}


function mkCurrentInvolvements(involvements = []) {
    return _.chain(involvements)
        .flatMap(i => {
            const personEntityRef = mkEntityRef(i.person);
            return _.map(i.involvements, inv => ({
                entity: personEntityRef,
                involvement: +inv.kindId,
                isReadOnly: inv.provenance !== 'waltz'
            }));
        })
        .value();
}


function controller(displayNameService) {

    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        vm.gridData = mkGridData(vm.involvements, displayNameService);
        vm.currentInvolvements = mkCurrentInvolvements(vm.involvements);

        vm.allowedInvolvements = _.map(
            displayNameService.getAllByType('involvementKind'),
            (name, id) => ({ value: +id, name }));
    };

    vm.columnDefs = [
        {
            field: 'person.displayName',
            displayName: 'Name',
            cellTemplate: '<div class="ui-grid-cell-contents"> <a ui-sref="main.person.view ({empId: row.entity.person.employeeId})" ng-bind="COL_FIELD"></a> - <a href="mailto:{{row.entity.person.email}}"><waltz-icon name="envelope-o"></waltz-icon></a></div>'
        },
        { field: 'person.title', displayName: 'Title' },
        { field: 'person.officePhone', displayName: 'Telephone' },
        { field: 'roles', displayName: 'Roles' }
    ];

    vm.onGridInitialise = (e) => {
        vm.exportGrid = () => e.exportFn("people.csv");
    };

    vm.onGridChange = (e) => {
        vm.gridDataCount = e.entriesCount;
    };

    vm.editMode = (editMode) => {
        vm.visibility.editor = editMode;
    };
}


controller.$inject = [
    'DisplayNameService'
];


const component = {
    bindings,
    template,
    controller
};

export default component;
