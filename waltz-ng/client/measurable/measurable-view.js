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

import _ from 'lodash';
import {initialiseData} from '../common';


const initialState = {

};


function controller($stateParams,
                    applicationStore,
                    assetCostViewService,
                    complexityStore,
                    logicalFlowViewService,
                    measurableStore,
                    measurableRatingStore,
                    sourceDataRatingStore,
                    technologyStatsService) {

    const id = $stateParams.id;
    const ref = { id, kind: 'MEASURABLE' };
    const parentsSelector = { entityReference: ref, scope: 'PARENTS' }
    const childrenSelector = { entityReference: ref, scope: 'CHILDREN' }

    const vm = initialiseData(this, initialState);

    measurableStore
        .findMeasurablesBySelector(parentsSelector)
        .then(ps => {
            vm.parents = ps;
            vm.measurable = _.find(ps, { id });
        });

    applicationStore
        .findBySelector(childrenSelector)
        .then(apps => vm.applications = apps);

    measurableStore
        .findMeasurablesBySelector(childrenSelector)
        .then(cs => vm.children = cs);

    measurableRatingStore
        .findByMeasurableSelector(childrenSelector)
        .then(rs => vm.ratings = rs);

    logicalFlowViewService
        .initialise(childrenSelector)
        .then(flowView => vm.logicalFlowView = flowView);

    complexityStore
        .findBySelector(childrenSelector)
        .then(complexity => vm.complexity = complexity);

    assetCostViewService
        .initialise(childrenSelector, 2016)
        .then(costs => vm.assetCostData = costs);

    technologyStatsService
        .findBySelector(childrenSelector)
        .then(techStats => vm.techStats = techStats);

    sourceDataRatingStore
        .findAll()
        .then(sourceDataRatings => vm.sourceDataRatings = sourceDataRatings);

}


controller.$inject = [
    '$stateParams',
    'ApplicationStore',
    'AssetCostViewService',
    'ComplexityStore',
    'LogicalFlowViewService',
    'MeasurableStore',
    'MeasurableRatingStore',
    'SourceDataRatingStore',
    'TechnologyStatisticsService'

];


export default {
    template: require('./measurable-view.html'),
    controller,
    controllerAs: 'ctrl'
};
