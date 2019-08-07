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
import template from './rating-schemes-view.html';
import {CORE_API} from "../common/services/core-api-utils";
import {initialiseData} from "../common";
import * as _ from "lodash";


const bindings = {};

const initialState = {
    ratingSchemes: [],
    assessmentDefinitions: [],
    measurableCategories: []
};


function controller(serviceBroker, $q) {

    const vm = initialiseData(this, initialState);

    const ratingSchemePromise = serviceBroker
        .loadViewData(
            CORE_API.RatingSchemeStore.findAll)
        .then(r => vm.ratingSchemes =
            _.forEach( r.data,
                d => _.forEach(
                    d.ratings,
                        rating => rating.name = rating.name + " (" + rating.rating + ")")));

    const assessmentDefinitionPromise = serviceBroker
        .loadViewData(
            CORE_API.AssessmentDefinitionStore.findAll)
        .then(r => vm.assessmentDefinitions = r.data);

    const measurableCategoryPromise = serviceBroker
        .loadViewData(
            CORE_API.MeasurableCategoryStore.findAll)
        .then(r => vm.measurableCategories = r.data);

    const roadmapsPromise = serviceBroker
        .loadViewData(
            CORE_API.RoadmapStore.findAllRoadmapsAndScenarios)
        .then(r => vm.roadmapsAndScenarios = r.data);

    $q.all([ratingSchemePromise, assessmentDefinitionPromise, measurableCategoryPromise, roadmapsPromise])
        .then(([ratingSchemes, assessmentDefinitions, measurableCategories, roadmaps]) => {
            vm.assessmentsKey = _.map(assessmentDefinitions, r => Object.assign({}, r, {type: "ASSESSMENT", icon: getIcon(r.entityKind)}));
            vm.measurablesKey = _.map(measurableCategories, r => Object.assign({}, r, {type: "MEASURABLE", icon: "puzzle-piece"}));
            vm.roadmapsKey = _.map(roadmaps, r => Object.assign({}, r, {type: "ROADMAP", icon: "road"}));

            vm.rateableEntities = _.concat(vm.assessmentsKey, vm.measurablesKey, vm.roadmapsKey)
        });

    vm.selectScheme = (ratingScheme) => {

        ratingScheme.ratings = _.orderBy(ratingScheme.ratings, ["position", "name"]);
        vm.selectedRatingScheme = ratingScheme;

        vm.usages = _.chain(vm.rateableEntities)
            .filter(a => ((a.type === "ROADMAP")
                ? a.roadmap.ratingSchemeId === vm.selectedRatingScheme.id
                : a.ratingSchemeId === vm.selectedRatingScheme.id ))
            .value();
    }
}


function getIcon(entityKind) {
    switch(entityKind) {
        case "APPLICATION":
            return "desktop";
        case "CHANGE_INITIATIVE":
            return "paper-plane-o";
        case "CHANGE_UNIT":
            return "hourglass";
        default:
            return null;
    }
}

controller.$inject = [
    "ServiceBroker",
    "$q"
];


export default {
    template,
    controller,
    controllerAs: "$ctrl",
    bindToController: true,
};