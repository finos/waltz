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
import {parseMeasurableListResponse} from "../../survey-utils";


const bindings = {
    instanceId: "<",
    question: "<",
    responses: "<?",
    onSaveListResponse: "<?",
    required: "<?"
}

const initialState = {
    onSaveListResponse: (d) => console.log("onAdd", d),
    required: false,
    responses: []
};

function toListResponse(itemIds, measurablesById) {
    return _
        .chain(itemIds)
        .map(id => _.get(measurablesById, id, null))
        .filter(m => m != null)
        .map(m => `${m.name} (${m.externalId}:MEASURABLE/${m.id})`)
        .value();
}

function controller(serviceBroker) {

    const vm = initialiseData(this, initialState);


    vm.$onChanges = () => {

        serviceBroker
            .loadViewData(CORE_API.MeasurableStore.findMeasurablesBySelector,
                          [mkSelectionOptions(
                              vm.question.qualifierEntity,
                              "EXACT")])
            .then(r => {
                vm.measurableItems = r.data;
                vm.measurablesById = _.keyBy(vm.measurableItems, d => d.id);


                const {measurableIds, notFoundSiphon} = parseMeasurableListResponse(
                    vm.responses,
                    vm.measurablesById);

                vm.checkedItemIds = measurableIds;
                vm.notFoundResults = _.map(notFoundSiphon.results, d => d.input);
            });
    }

    vm.isDisabled = (d) => !d.concrete;

    function updateItems() {
        const listResponse = toListResponse(vm.checkedItemIds, vm.measurablesById);
        if (vm.notFoundResults.length > 0) {
            const message = "Removing the following items as they cannot be found: "
                + _.join(vm.notFoundResults, ", ");

            serviceBroker
                .execute(
                    CORE_API.SurveyInstanceStore.reportProblemWithQuestionResponse,
                    [vm.instanceId, vm.question.id, message])
                .then(() => vm.notFoundResults = []);
        }
        vm.onSaveListResponse(vm.question.id, listResponse);
    }

    vm.onItemCheck = (d) => {
        vm.checkedItemIds = _.union(vm.checkedItemIds, [d]);
        updateItems();
    };

    vm.onItemUncheck = (d) => {
        vm.checkedItemIds = _.without(vm.checkedItemIds, d);
        updateItems();
    };
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