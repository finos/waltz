/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
import {initialiseData} from "../common";
import {getParents, populateParents} from "../common/hierarchy-utils";

import template from "./measurable-view.html";
import {CORE_API} from "../common/services/core-api-utils";


const initialState = {

};


function logHistory(measurable, historyStore) {
    return historyStore
        .put(measurable.name,
            'MEASURABLE',
            'main.measurable.view',
            { id: measurable.id });
}


function controller($q,
                    $stateParams,
                    serviceBroker,
                    historyStore) {

    const id = $stateParams.id;
    const ref = { id, kind: 'MEASURABLE' };
    const childrenSelector = { entityReference: ref, scope: 'CHILDREN' }

    const vm = initialiseData(this, initialState);
    vm.entityReference = ref;
    vm.scope = childrenSelector.scope;


    // -- LOAD ---

    const loadWave1 = () =>
        $q.all([
            serviceBroker.loadAppData(CORE_API.MeasurableStore.findAll, [])
                .then(result => {
                    const all = result.data;
                    vm.allMeasurables = all;
                    const withParents = populateParents(all);
                    vm.measurable = _.find(withParents, { id });
                    vm.entityReference = Object.assign({}, vm.entityReference, { name: vm.measurable.name});
                    vm.parents = getParents(vm.measurable);
                    vm.children = _.chain(all)
                        .filter({ parentId: id })
                        .sortBy('name')
                        .value();
                }),
        ]);

    const loadWave2 = () =>
        $q.all([
            serviceBroker.loadAppData(CORE_API.MeasurableCategoryStore.findAll, [])
                .then(result => {
                    const cs = result.data;
                    vm.measurableCategories = cs;
                    vm.measurableCategory = _.find(cs, { id: vm.measurable.categoryId });
                }),
            serviceBroker
                .loadViewData(CORE_API.MeasurableRatingStore.statsForRelatedMeasurables, [id])
                .then(r => vm.relatedStats = r.data),
            serviceBroker
                .loadViewData(CORE_API.ApplicationStore.findBySelector, [childrenSelector])
                .then(r => vm.applications = r.data),
            serviceBroker
                .loadViewData(CORE_API.TechnologyStatisticsService.findBySelector, [childrenSelector])
                .then(r => vm.techStats = r.data)
        ]);


    const loadWave3 = () => logHistory(vm.measurable, historyStore);

    // -- BOOT ---

    loadWave1()
        .then(loadWave2)
        .then(loadWave3);
}


controller.$inject = [
    '$q',
    '$stateParams',
    'ServiceBroker',
    'HistoryStore'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};
