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


const BINDINGS = {
    involvements: '=',
    sourceDataRatings: '='
};


const initialState = {
    involvements: []
};


const personComparator = (a, b) => {
    const aName = a.displayName;
    const bName = b.displayName;
    if (aName === bName) return 0;
    return aName > bName
        ? 1
        : -1;
};


const columnDefs = [
    {
        field: 'person',
        displayName: 'Name',
        sortingAlgorithm: personComparator,
        filter: {
            condition: (searchTerm, cellValue) => {
                const name = cellValue.displayName;
                return name.match(new RegExp(searchTerm, 'i'));
            }
        },
        cellTemplate: '<div class="ui-grid-cell-contents"> <a ui-sref="main.person.view ({empId: COL_FIELD.employeeId})" ng-bind="COL_FIELD.displayName CUSTOM_FILTERS"></a> - <a href="mailto:{{COL_FIELD.email}}"><waltz-icon name="envelope-o"></waltz-icon></a></div>'
    },
    { field: 'person.title', displayName: 'Title' },
    { field: 'person.officePhone', displayName: 'Telelphone' },
    {
        field: 'involvements',
        displayName: 'Roles',
        cellTemplate: '<div class="ui-grid-cell-contents"><span ng-repeat="role in COL_FIELD"><span ng-bind="role | toDisplayName:\'involvementKind\'"></span><span ng-if="!$last">, </span></span></div>'
    }
];



function controller($scope, uiGridConstants) {

    const vm = _.defaults(this, initialState);

    vm.gridOptions = {
        enableSorting: true,
        enableFiltering: true,
        enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
        columnDefs,
        data: []
    };

    $scope.$watch(
        'ctrl.involvements',
        (involvements) => vm.gridOptions.data = involvements);
}

controller.$inject = ['$scope', 'uiGridConstants'];


const directive = {
    restrict: 'E',
    replace: true,
    template: require('./involved-people-section.html'),
    scope: {},
    bindToController: BINDINGS,
    controller,
    controllerAs: 'ctrl'
};


export default () => directive;
