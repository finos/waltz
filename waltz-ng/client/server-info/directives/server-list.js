

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

const BINDINGS = {
    servers: '=',
    primaryAssetCode: '=?'
};

function controller(uiGridConstants) {
    const vm = this;



    vm.gridOptions = {
        enableSorting: true,
        enableFiltering: true,
        enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
        columnDefs: [
            { field: 'hostname' },
            { field: 'environment' },
            {
                field: 'virtual',
                displayName: 'Virtual',
                filter: {
                    type: uiGridConstants.filter.SELECT,
                    selectOptions: [
                        { value: 'true', label: 'Yes' },
                        { value: 'false', label: 'No' }
                    ]
                },
                cellTemplate: '<div class="ui-grid-cell-contents"> <waltz-icon ng-if="COL_FIELD" name="check"></waltz-icon></div>'
            },
            { field: 'operatingSystem' },
            { field: 'operatingSystemVersion', displayName: 'Version' },
            { field: 'location' },
            { field: 'country' }
        ],
        data: vm.servers
    };

}

controller.$inject = ['uiGridConstants'];


import template from './server-list.html';

export default () => ({
    restrict: 'E',
    replace: true,
    scope: {},
    template,
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller
});
