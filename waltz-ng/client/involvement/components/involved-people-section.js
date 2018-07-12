/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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

import template from "./involved-people-section.html";
import {CORE_API} from "../../common/services/core-api-utils";
import {aggregatePeopleInvolvements} from "../involvement-utils";


const bindings = {
    parentEntityRef: '<',
};


const initialState = {
    allowedInvolvements: [],
    currentInvolvements: [],
    gridData: [],
    gridDataCount: 0,
    exportGrid: () => {},
    visibility: {
        editor: false
    }
};


function mkGridData(involvements = [], displayNameService) {
    return _.chain(involvements)
        .map(inv => {
            const roles = _.map(inv.involvements, ik => ({
                    provenance: ik.provenance,
                    displayName: displayNameService.lookup('involvementKind', ik.kindId)
                }));

            const rolesDisplayName = _.chain(roles)
                .map('displayName')
                .join(', ')
                .value();

            return {
                person: inv.person,
                roles,
                rolesDisplayName
            }
        })
        .sortBy('person.displayName')
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


function controller($q, displayNameService, serviceBroker, involvedSectionService) {

    const vm = initialiseData(this, initialState);

    const refresh = () => {
        const involvementPromise = serviceBroker
            .loadViewData(
                CORE_API.InvolvementStore.findByEntityReference,
                [ vm.parentEntityRef ],
                { force: true })
            .then(r => r.data);

        const peoplePromise = serviceBroker
            .loadViewData(
                CORE_API.InvolvementStore.findPeopleByEntityReference,
                [ vm.parentEntityRef ],
                { force: true })
            .then(r => r.data);

        $q.all([involvementPromise, peoplePromise])
            .then(([involvements = [], people = []]) => {
                const aggInvolvements = aggregatePeopleInvolvements(involvements, people);
                vm.gridData = mkGridData(aggInvolvements, displayNameService);
                vm.currentInvolvements = mkCurrentInvolvements(aggInvolvements);
            });
    };


    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.InvolvementKindStore.findAll, [])
            .then(r => vm.involementKinds = r.data);
    };

    vm.$onChanges = (changes) => {
        if (vm.parentEntityRef) {
            refresh();
        }


        vm.allowedInvolvements = _.map(
            displayNameService.getAllByType('involvementKind'),
            (name, id) => ({ value: +id, name }));
    };

    vm.columnDefs = [
        {
            field: 'person.displayName',
            displayName: 'Name',
            cellTemplate: `
                <div class="ui-grid-cell-contents"> 
                    <a ui-sref="main.person.view ({empId: row.entity.person.employeeId})" ng-bind="COL_FIELD"></a> - 
                    <a href="mailto:{{row.entity.person.email}}">
                        <waltz-icon name="envelope-o"></waltz-icon>
                    </a>
                </div>`
        },
        { field: 'person.title', displayName: 'Title' },
        { field: 'person.officePhone', displayName: 'Telephone' },
        {
            field: 'rolesDisplayName',
            displayName: 'Roles',
            sortingAlgorithm: (a, b) => {
                const aNames = _.join(_.map(a, 'displayName'));
                const bNames = _.join(_.map(b, 'displayName'));
                return aNames.localeCompare(bNames);
            },
            cellTemplate: `
                <div class="ui-grid-cell-contents"> 
                    <span ng-bind="COL_FIELD" 
                          uib-popover-template="'wips/roles-popup.html'"
                          popover-trigger="mouseenter"
                          popover-append-to-body="true">
                    </span>   
                </div>`
        }
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


    vm.onAdd = (entityInvolvement) => {
        involvedSectionService
            .addInvolvement(vm.parentEntityRef, entityInvolvement)
            .then(refresh);
    };


    vm.onRemove = (entityInvolvement) => {
        involvedSectionService
            .removeInvolvement(vm.parentEntityRef, entityInvolvement)
            .then(refresh);
    };
}


controller.$inject = [
    '$q',
    'DisplayNameService',
    'ServiceBroker',
    'InvolvedSectionService'
];


const component = {
    bindings,
    template,
    controller
};

export default component;
