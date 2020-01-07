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

import template from "./assessment-rating-summary-pies.html";
import { initialiseData, invokeFunction } from "../../../common";
import { color } from "d3-color";


const bindings = {
    summaries: "<",
    selectedSummary: "<",
    selectedRating: "<",
    helpText: "@?",
    onSelectSummary: "<?",
    onSelectSegment: "<?" // fn, data looks like:  `{ assessmentId, ratingId }` or `null` if cleared
};

const basePieConfig = {
    labelProvider: d => _.get(d, ["rating", "name"], "Other"),
    valueProvider: d => d.count,
    colorProvider: d => color(_.get(d, ["rating", "color"], "#bbbbbb")),
    descriptionProvider: d => _.get(d, ["rating", "description"], "Other")
};

const initialState = {
    summaries: [],
    helpText: "Relevant assessments",
    selectedSummary: null
};


function controller() {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        vm.listConfig = Object.assign({}, basePieConfig, { size: 24 });
        vm.selectedConfig = Object.assign({}, basePieConfig, { size: 48, onSelect: vm.onSelectRatingItem });
    };

    vm.onSelectGroup = (s) => {
        if (vm.onSelectSummary) {
            vm.onSelectSummary(s);
        } else {
            vm.selectedSummary = s;
        }
    };

    vm.onCloseSummary = () => {
        vm.onSelectGroup(null);
        vm.onSelectRatingItem(null);
    };

    vm.onSelectRatingItem = (d) => {
        invokeFunction(vm.onSelectSegment, d);
    };

}

controller.$inject = [];


const component = {
    template,
    controller,
    bindings
};


export default {
    id: "waltzAssessmentRatingSummaryPies",
    component
}