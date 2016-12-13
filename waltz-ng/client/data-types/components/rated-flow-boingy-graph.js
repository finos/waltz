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

import {initialiseData} from "../../common";
import {authoritativeRatingColorScale} from "../../common/colors";
import _ from "lodash";


const bindings = {
    data: '<',
    dataTypes: '<',
    typeId: '<'
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


const template = require('./rated-flow-boingy-graph.html');


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


function controller($scope) {
    const vm = initialiseData(this, initialState);
    const onAppSelect = (app) => $scope.$applyAsync(() => vm.selectedApp = app);

    vm.filterChanged = () => {
        const filteredData = filterData(
            vm.rawFlows,
            vm.rawDecorators,
            vm.filterOptions);
        vm.flowData = filteredData;
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

    vm.$onChanges = () => {
        const rawFlows = vm.data
            ? vm.data.flows
            : [];
        const rawDecorators = vm.data
            ? vm.data.decorators
            : [];

        vm.graphTweakers = buildGraphTweakers(
            rawDecorators,
            onAppSelect);

        vm.rawFlows = rawFlows;
        vm.rawDecorators = rawDecorators;
        vm.filterChanged();
    };

    vm.refocusApp = app => {
        onAppSelect(app);
    };
}


controller.$inject = [
    '$scope'
];


const component = {
    bindings,
    template,
    controller
};


export default component;