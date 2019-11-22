/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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
    return a.definition.visibility !== "PRIMARY";
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
        vm.visibility.showPrimaryToggle = _.some(vm.assessments, isPrimary);
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
