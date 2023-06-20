/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
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

        // QUESTION: can we do this without making the full call?  Only used to hide/show implicit measurables
        const selectionOptions = mkSelectionOptions(vm.parentEntityRef);

        const measurablesPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll)
            .then(r => vm.measurables = r.data);

        const statsPromise = serviceBroker
            .loadViewData(
                CORE_API.MeasurableRatingStore.statsForRelatedMeasurables,
                [ selectionOptions ])
            .then(r => vm.stats = r.data);

        const promises = [
            measurablesPromise,
            statsPromise
        ];

        $q
            .all(promises)
            .then(() => vm.relatedMeasurables = calcRelatedMeasurables(vm.stats, vm.measurables));

    };
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


export default {
    component,
    id: "waltzRelatedMeasurablesSection"
};
