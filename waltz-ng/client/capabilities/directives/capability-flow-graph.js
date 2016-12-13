/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";


function controller($scope) {

    const vm = this;

    vm.selectedType = 'ALL';

    vm.tweakers = {
        node: {
            enter: (selection) => {
                selection
                    .on('click', app => app.fixed = true)
                    .on('dblclick', app => app.fixed = false)
                    .classed('waltz-capability-node-member', app => app.member)
                    .classed('waltz-capability-node-non-member', app => ! app.member);
            }

        }
    };


    function calculateFlowData(flows, memberAppIds) {
        const entities = _.chain(flows)
            .map(df => [df.source, df.target])
            .flatten()
            .uniqBy('id')
            .map(app => ({...app, member: _.includes(memberAppIds, app.id)}))
            .value();

        return {
            flows,
            entities
        };
    }

    vm.filterByDataType = (dataType) => {
        const memberAppIds = _.map(vm.appsWithCapability, 'id');

        const flows = dataType === 'ALL'
            ? vm.flows
            : _.filter(vm.flows, f => f.dataType === dataType);

        vm.flowData = calculateFlowData(flows, memberAppIds);
    };

    $scope.$watchGroup(['ctrl.appsWithCapability', 'ctrl.flows'], () => {
        const memberAppIds = _.map(vm.appsWithCapability, 'id');
        vm.flowData = calculateFlowData(vm.flows, memberAppIds);
        vm.availableDataTypes = _.chain(vm.flows)
            .map('dataType')
            .uniq()
            .value();

    });

}

controller.$inject = ['$scope'];


function directive() {
    return {
        restrict: 'E',
        replace: true,
        template: require('./capability-flow-graph.html'),
        scope: {},
        controller,
        controllerAs: 'ctrl',
        bindToController: {
            flows: '=',
            appsWithCapability: '='
        }
    };
}


export default directive;
