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

import {initialiseData} from "../../../common";
import {authoritativeRatingColorScale} from "../../../common/colors";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";

import template from './data-type-flow-section.html';


const bindings = {
    parentEntityRef: '<'
};


const initialState = {
    flowData: {},
    filterOptions: {
        showPrimary: true,
        showSecondary: true,
        showDiscouraged: false,
        showNoOpinion: false
    }
};


function calculateEntities(flows = []) {
    return _.chain(flows)
        .flatMap(f => [f.source, f.target])
        .uniqBy("id")
        .value();
}


const buildGraphTweakers = (decorators = [],
                            onAppSelect) => {
    const decoratorsByFlowId = _.keyBy(decorators, 'dataFlowId');

    const getRatingForLink = (link) => {
        const decorator = decoratorsByFlowId[link.data.id];
        return decorator
            ? decorator.rating
            : 'NO_OPINION';
    };

    return {
        node : {
            enter: (selection) => {
                selection
                    .on('click.appSelect', onAppSelect)
                    .on('dblclick.unfix', d => { d.fx = null; d.fy = null; })
            },
            exit: _.identity,
            update: _.identity
        },
        link : {
            enter: (selection) => {
                selection
                    .attrs({
                        stroke: (d) => {
                            const rating = getRatingForLink(d);
                            return authoritativeRatingColorScale(rating);
                        },
                        fill: (d) => {
                            const rating = getRatingForLink(d);
                            return authoritativeRatingColorScale(rating).brighter();
                        }
                    });
            },
            exit: _.identity,
            update: _.identity
        }
    };
};


function mkKeepDecoratorFn(filterOptions = {}) {
    return (decorator) => {
        const rating = decorator.rating;
        switch (rating) {
            case 'PRIMARY':
                return filterOptions.showPrimary;
            case 'SECONDARY':
                return filterOptions.showSecondary;
            case 'DISCOURAGED':
                return filterOptions.showDiscouraged;
            case 'NO_OPINION':
                return filterOptions.showNoOpinion;
        }
    };
}


function filterDecorators(decorators =[],
                          filterOptions) {
    return _.filter(decorators, mkKeepDecoratorFn(filterOptions));
}


function filterFlows(flows = [],
                     decorators = []) {
    const flowIds = _.map(decorators, 'dataFlowId');
    return _.filter(flows, f => _.includes(flowIds, f.id));

}


function filterData(flows = [],
                    decorators = [],
                    filterOptions) {
    const filteredDecorators = filterDecorators(decorators, filterOptions);
    const filteredFlows = filterFlows(flows, filteredDecorators);
    const filteredEntities = calculateEntities(filteredFlows);
    return {
        entities: filteredEntities,
        flows: filteredFlows,
        decorators: filteredDecorators
    };
}


function controller($q, $scope, serviceBroker) {
    const vm = initialiseData(this, initialState);
    const onAppSelect = (app) => $scope.$applyAsync(() => vm.selectedApp = app);

    vm.filterChanged = () => {
        vm.flowData = filterData(
            vm.rawFlows,
            vm.rawDecorators,
            vm.filterOptions);
    };

    vm.showAll = () => {
        vm.filterOptions = {
            showPrimary: true,
            showSecondary: true,
            showDiscouraged: true,
            showNoOpinion: true
        };
        vm.filterChanged();
    };

    vm.refocusApp = app => {
        onAppSelect(app);
    };

    vm.$onInit = () => {
        const selector = mkSelectionOptions(vm.parentEntityRef);
        const flowPromise = serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.findBySelector,
                [ selector ])
            .then(r => vm.rawFlows = r.data);
        const decoratorPromise = serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowDecoratorStore.findBySelector,
                [ selector ])
            .then(r => {
                vm.rawDecorators = r.data;
                vm.graphTweakers = buildGraphTweakers(
                    vm.rawDecorators,
                    onAppSelect);
            });

        $q.all([flowPromise, decoratorPromise])
            .then(() => vm.filterChanged());
    };
}


controller.$inject = [
    '$q',
    '$scope',
    'ServiceBroker'
];


const component = {
    bindings,
    template,
    controller
};


const id = 'waltzDataTypeFlowSection'

export default {
    component,
    id
};