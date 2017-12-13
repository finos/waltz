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
import {mkOverrides} from "../../perpective-utilities";
import {CORE_API} from '../../../common/services/core-api-utils';
import {initialiseData} from '../../../common/index';


import template from "./perspective-rating-panel.html";


const bindings = {
    parentEntityRef: '<',
    measurableCategory: '<', // measurable to filter perspectives to only those that have this measurable
};


const initialState = {
    application: null,
    selectedPerspectiveBlock: null,
    perspectiveDefinitions: [],
    perspectiveRatings: [],
    measurables: [],
    measurableRatings: [],
};


function controller($q,
                    serviceBroker) {

    const vm = initialiseData(this, initialState);


    vm.$onInit = () => {

        const idSelector = {
            entityReference: vm.parentEntityRef,
            scope: 'EXACT'
        };


        serviceBroker
            .loadViewData(CORE_API.ApplicationStore.getById, [vm.parentEntityRef.id])
            .then(r => {
                const app = r.data;
                vm.application = app;
                vm.parentEntityRef = Object.assign({}, vm.parentEntityRef, { name: app.name });
            });

        const defnPromise = serviceBroker
            .loadViewData(CORE_API.PerspectiveDefinitionStore.findAll)
            .then(r => {
                vm.perspectives = _.filter(
                    r.data,
                    pd => pd.categoryX === vm.measurableCategory.id || pd.categoryY === vm.measurableCategory.id);
            });

        const ratingPromise = serviceBroker
            .loadViewData(CORE_API.PerspectiveRatingStore.findForEntity, [vm.parentEntityRef])
            .then(r => vm.perspectiveRatings = r.data);

        const measurablePromise = serviceBroker
            .loadViewData(CORE_API.MeasurableStore.findMeasurablesBySelector, [idSelector])
            .then(r => vm.measurables = r.data);

        serviceBroker
            .loadViewData(CORE_API.MeasurableRatingStore.findByAppSelector, [idSelector])
            .then(r => vm.measurableRatings = r.data);

        $q.all([ratingPromise, measurablePromise, defnPromise])
            .then(() => {
                vm.perspectiveBlocks = _
                    .chain(vm.perspectives)
                    .map(p => {
                        return {
                            definition: p,
                            showImplied: false,
                            measurablesX: _.filter(vm.measurables, { categoryId: p.categoryX }),
                            measurablesY: _.filter(vm.measurables, { categoryId: p.categoryY })
                        };
                    })
                    .map(pb => {
                        const relevantMeasurableIds = _.union(_.map(pb.measurablesX, "id"), _.map(pb.measurablesY, "id"));
                        const relevantRatings = _.filter(vm.perspectiveRatings, r => {
                            const xRelevant = _.includes(relevantMeasurableIds, r.value.measurableX);
                            const yRelevant = _.includes(relevantMeasurableIds, r.value.measurableY);
                            return xRelevant && yRelevant;
                        });
                        const overrides = mkOverrides(relevantRatings);
                        const result = Object.assign({}, pb, { overrides });
                        return result;
                    })
                    .value();

                vm.selectedPerspectiveBlock = _.head(vm.perspectiveBlocks);
            });
    };

    vm.hasMeasurables = (perspectiveBlock) => {
        return perspectiveBlock.measurablesX.length > 0
            && perspectiveBlock.measurablesY.length > 0;
    };

    vm.hasPerspectiveOverrides = (perspectiveBlock) => {
        return _.keys(perspectiveBlock.overrides).length > 0;
    };

    vm.hasNoPerspectiveRatings = () => {
        return _.every(vm.perspectiveBlocks, (pb) => !(vm.hasMeasurables(pb) && vm.hasPerspectiveOverrides(pb)));
    };
}


controller.$inject = [
    '$q',
    'ServiceBroker',
];


const component = {
    bindings,
    controller,
    template
};


export default {
    component,
    id: 'waltzPerspectiveRatingPanel'
};