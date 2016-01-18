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

import _ from 'lodash';

import { lifecyclePhaseDisplayNames, applicationKindDisplayNames }
    from '../../common/services/display_names';


function controller(uiGridConstants, $scope) {

    const vm = this;

    vm.gridOptions = {
        enableSorting: true,
        enableFiltering: true,
        enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
        columnDefs: [
            {
                field: 'name',
                cellTemplate: '<div class="ui-grid-cell-contents"> <a ui-sref="main.app-view ({ id: row.entity[\'id\'] })">{{ COL_FIELD }}</a></div>'
            },
            {
                field: 'kind',
                cellTemplate: '<div class="ui-grid-cell-contents"> {{ COL_FIELD | toDisplayName:"applicationKind" }}</div>',
                filter: {
                    type: uiGridConstants.filter.SELECT,
                    selectOptions: _.map(applicationKindDisplayNames, (label, value) => ({ label, value }))
                }
            },
            {
                field: 'lifecyclePhase',
                cellTemplate: '<div class="ui-grid-cell-contents"> {{ COL_FIELD | toDisplayName:"lifecyclePhase" }}</div>',
                filter: {
                    type: uiGridConstants.filter.SELECT,
                    selectOptions: _.map(lifecyclePhaseDisplayNames, (label, value) => ({ label, value }))
                }
            },
            {
                field: 'description',
                cellTooltip: (row) => row.entity.description
            }
        ],
        data: vm.applications
    };

    $scope.$watch('ctrl.applications', (apps) => vm.gridOptions.data = apps);
}

controller.$inject = ['uiGridConstants', '$scope'];


export default () => {
    return {
        restrict: 'E',
        replace: true,
        template: require('./app-table.html'),
        scope: {},
        bindToController: {
            applications: '='
        },
        controllerAs: 'ctrl',
        controller
    };
};
