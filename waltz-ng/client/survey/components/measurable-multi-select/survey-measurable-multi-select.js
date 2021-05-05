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

import {initialiseData} from "../../../common";
import template from "./survey-measurable-multi-select.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {mkSiphon} from "../../../common/siphon-utils";


const bindings = {
    question: "<",
    responses: "<?",
    onRemoveMeasurable: "<?",
    onAddMeasurable: "<?"
}

const initialState = {
    onRemoveMeasurable: (d) => console.log("onRemove", d),
    onAddMeasurable: (d) => console.log("onAdd", d)
};

const findMeasurableIdRegEx = /MEASURABLE\/(\d+)\)$/;

function controller(serviceBroker) {

    const vm = initialiseData(this, initialState);


    vm.$onChanges = () => {

        serviceBroker
            .loadViewData(CORE_API.MeasurableStore.findMeasurablesBySelector,
                          [mkSelectionOptions(
                              vm.question.qualifierEntity,
                              "EXACT")])
            .then(r => {
                vm.measurableItems = console.log(r.data) || r.data;
                vm.measurablesById = _.keyBy(vm.measurableItems, d => d.id);

                const measurableIds = _.map(_.keys(vm.measurablesById), d => Number(d));
                const invalidItemStringSiphon = mkSiphon(d => !d.match(findMeasurableIdRegEx));
                const notFoundSiphon = mkSiphon(d => !_.includes(measurableIds, d.id));

                vm.checkedItemIds = _
                    .chain(vm.responses)
                    .reject(invalidItemStringSiphon)
                    .map(r => r.match(findMeasurableIdRegEx))
                    .map(m => ({id: Number(m[1]), input: m.input}))
                    .reject(notFoundSiphon)
                    .map(r => r.id)
                    .value();

                vm.notFoundResults = _.map(notFoundSiphon.results, d => d.input);

                console.log({checked: vm.checkedItemIds})
                console.log({invalidMatches: invalidItemStringSiphon.results, notFound: vm.notFoundResults})

            });
    }


    vm.isDisabled = (d) => !d.concrete;

    vm.onItemCheck = (d) => {
        vm.checkedItemIds = _.union(vm.checkedItemIds, [d]);
        const measurable = _.get(vm.measurablesById, d, null);
        vm.onAddMeasurable(measurable);
    }

    vm.onItemUncheck = (d) => {
        vm.checkedItemIds = _.without(vm.checkedItemIds, d);
        const measurable = _.get(vm.measurablesById, d, null);
        vm.onRemoveMeasurable(measurable);
    }
}


controller.$inject = [
    "ServiceBroker",
];


const component = {
    controller,
    template,
    bindings
};

export default {
    component,
    id: "waltzSurveyMeasurableMultiSelect"
};