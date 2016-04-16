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
            .uniq('id')
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
            .uniq(false)
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
