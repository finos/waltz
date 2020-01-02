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

import { initialiseData } from "../../../common";
import _ from "lodash";
import template from "./assessment-rating-list.html";


const bindings = {
    assessments: "<",
    onSelect: "<"
};


const initialState = {
    assessmentsWithRatings: [],
    assessmentsWithoutRatings: [],
    showPrimaryOnly: true,
    visibility: {
        showPrimaryToggle: false
    }
};


function isPrimary(a) {
    return a.definition.visibility === "PRIMARY";
}


function controller() {
    const vm = initialiseData(this, initialState);

    const filterAssessments = (primaryOnly) => {
        if (vm.assessments) {
            const filtered = _.filter(vm.assessments, a =>  primaryOnly
                ? isPrimary(a)
                : true);

            const valuePartitioned = _.partition(
                filtered,
                assessment => _.isNil(assessment.rating));
            vm.assessmentsWithoutRatings = _.sortBy(valuePartitioned[0], d => d.definition.name);
            vm.assessmentsWithRatings = _.sortBy(valuePartitioned[1], d => d.definition.name);
        }
    };

    vm.$onChanges = () => {
        vm.visibility.showPrimaryToggle = _.some(vm.assessments, a => !isPrimary(a));
        filterAssessments(vm.showPrimaryOnly);
    };

    vm.togglePrimaryOnly = () => {
        vm.showPrimaryOnly = !vm.showPrimaryOnly;
        filterAssessments(vm.showPrimaryOnly);
    };

}


controller.$inject = [
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzAssessmentRatingList"
};
