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
import {CORE_API} from "../../../common/services/core-api-utils";

import template from "./related-measurables-section.html";
import {mkSelectionOptions} from "../../../common/selector-utils";


/**
 * @name waltz-related-measurables-explorer
 *
 * @description
 * This component ...
 */
const bindings = {
    parentEntityRef: "<"
};


const initialState = {};


function calcRelatedMeasurables(ratingTallies = [], allMeasurables = []) {
    const relatedMeasurableIds = _.map(ratingTallies, "id");
    const measurablesById = _.keyBy(allMeasurables, "id");
    return _
        .chain(allMeasurables)
        .filter(m => _.includes(relatedMeasurableIds, m.id))
        .reduce(
            (acc, m) => {
                let ptr = m;
                while(ptr) {
                    acc[ptr.id] = ptr;
                    ptr = measurablesById[ptr.parentId];
                }
                return acc;
            },
            {})
        .values()
        .value();
}


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {

        if (! vm.parentEntityRef) {
            return;
        }

        const selectionOptions = mkSelectionOptions(vm.parentEntityRef);
        const categoriesPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
            .then(r => vm.categories = r.data);

        const measurablesPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll)
            .then(r => vm.measurables = r.data);

        const statsPromise = serviceBroker
            .loadViewData(
                CORE_API.MeasurableRatingStore.statsForRelatedMeasurables,
                [ selectionOptions ])
            .then(r => vm.stats = r.data);

        const promises = [
            categoriesPromise,
            measurablesPromise,
            statsPromise
        ];

        $q.all(promises)
          .then(() => vm.relatedMeasurables = calcRelatedMeasurables(vm.stats, vm.measurables));

    };

    vm.onSelect = (m) => vm.selectedMeasurable = m;
}


controller.$inject = [
    "$q",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default component;