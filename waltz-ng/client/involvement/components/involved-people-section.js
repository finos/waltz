/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */
import _ from "lodash";
import {initialiseData} from "../../common";


const bindings = {
    involvements: '<',
    sourceDataRatings: '<'
};


const template = require('./involved-people-section.html');


const initialState = {
    involvements: [],
    gridData: [],
    gridDataCount: 0,
    exportGrid: () => {}
};


function mkGridData(involvements = [], displayNameService) {
    return _.chain(involvements)
        .map(inv => {
            const roles = _.join(
                _.map(inv.involvements, ik => displayNameService.lookup('involvementKind', ik)),
                ', '
            );

            return {
                person: inv.person,
                roles: roles
            }
        })
        .value();
}


function controller(displayNameService) {

    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        vm.gridData = mkGridData(vm.involvements, displayNameService);
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

    vm.dismissSourceDataOverlay = () => {
        vm.sourceDataOverlay = false;
    }
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
