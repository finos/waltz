

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
import d3 from 'd3';

import { environmentColorScale, operatingSystemColorScale, variableScale } from '../../common/colors';


/**
 * the d3 nest function aggregates using the property name 'values', this
 * function creates a copy of the data with the name 'count'.
 *
 * @param data
 * @returns {Array|*}
 */
function toPieData(data) {
    return _.map(data, d => ({ key: d.key, count: d.values }));
}

function controller($scope, uiGridConstants) {
    const vm = this;


    vm.pie = {
        env: {
            config: {
                colorProvider: (d) => environmentColorScale(d.data.key)
            }
        },
        os: {
            config: {
                colorProvider: (d) => operatingSystemColorScale(d.data.key)
            }
        },
        location: {
            config: {
                colorProvider: (d) => variableScale(d.data.key)
            }
        }
    };

    function update(servers) {
        if (!servers) return;

        vm.pie.env.data = toPieData(d3.nest()
            .key(d => d.environment)
            .rollup(d => d.length)
            .entries(servers));

        vm.pie.os.data = toPieData(d3.nest()
            .key(d => d.operatingSystem)
            .rollup(d => d.length)
            .entries(servers));

        vm.pie.location.data = toPieData(d3.nest()
            .key(d => d.location)
            .rollup(d => d.length)
            .entries(servers));
    }

    $scope.$watch('ctrl.servers', (servers) => update(servers), true);

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
    scope: {
        servers: '=',
        primaryAssetCode: '=?'
    },
    template: require('./server-list.html'),
    bindToController: true,
    controllerAs: 'ctrl',
    controller
});
