

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

const BINDINGS = {
    servers: '=',
    primaryAssetCode: '=?'
};

function controller($scope, uiGridConstants) {
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

controller.$inject = ['$scope', 'uiGridConstants'];


export default () => ({
    restrict: 'E',
    replace: true,
    scope: {},
    template: require('./server-list.html'),
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller
});
