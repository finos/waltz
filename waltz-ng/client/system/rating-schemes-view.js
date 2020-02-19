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
import template from "./rating-schemes-view.html";
import {CORE_API} from "../common/services/core-api-utils";
import {initialiseData} from "../common";
import * as _ from "lodash";


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
            _.forEach( r.data, d => _.forEach(
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