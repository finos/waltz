
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
    entities: '=',
    flows: '=',
    tweakers: '='
};

function controller($scope) {
    const rawData = {
        flows: [],
        entities: []
    };
    const vm = this;

    $scope.$watchGroup(
        ['ctrl.entities', 'ctrl.flows'],
        ([entities, flows]) => {

        if (entities && flows ) {
            rawData.entities = entities;
            rawData.flows = flows;
            rawData.entitiesById = _.keyBy(entities, 'id');

            vm.currentData = { ...rawData };

            const availableTypes = _.chain(flows)
                .map('dataType')
                .uniq()
                .value();

            vm.types = {
                available: availableTypes,
                selected: 'ALL'
            };

        }

    });


    const filterFlows = (type) => {
        const flows = _.filter(
            rawData.flows,
            f => type === 'ALL' ? true : f.dataType === type);

        const entities = _.chain(flows)
            .map(f => ([f.source.id, f.target.id]))
            .flatten()
            .uniq()
            .map(id => rawData.entitiesById[id])
            .value();

        vm.currentData = {
            flows,
            entities
        };

    };

    vm.show = (type) => {
        vm.types.selected = type;
        filterFlows(type);
    };
 }


controller.$inject = ['$scope'];


export default [
    () => ({
        restrict: 'E',
        replace: true,
        template: require('./org-unit-flow-viz.html'),
        scope: {},
        controllerAs: 'ctrl',
        controller,
        bindToController: BINDINGS
    })
];
