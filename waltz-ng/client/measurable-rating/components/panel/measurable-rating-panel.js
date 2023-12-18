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
import template from "./measurable-rating-panel.html";
import MeasurableRatingTable from "../../svelte/MeasurableRatingTable.svelte";
import {selectedMeasurable} from "./measurable-rating-panel-store";

/**
 * @name waltz-measurable-rating-panel
 *
 * @description
 * This component wraps a <code>measurable-rating-tree</code>
 * and provides a detail view when a node is clicked on.
 *
 * It is intended to be used when viewing a single application
 * and a single 'kind' of measurables.
 */


const bindings = {
    allocations: "<",
    allocationSchemes: "<",
    measurables: "<",
    ratings: "<",
    ratingSchemeItems: "<",
    plannedDecommissions: "<?",
    replacingDecommissions: "<?",
    replacementApps: "<?",
    application: "<",
    assessmentDefinitions: "<?",
    assessmentRatings: "<?",
};


const initialState = {
    selected: null,
    plannedDecommissions: [],
    replacingDecommissions: [],
    replacementApps: [],
    assessmentRatings: [],
    assessmentDefinitions: [],
    showTreeView: false,
    MeasurableRatingTable
};


function enrichAllocationsWithScheme(node, allocationSchemesById) {
    return _.map(
        node.allocations,
        a => Object.assign({}, a, {scheme: allocationSchemesById[a.schemeId]}));
}


function controller($scope) {
    const vm = this;

    vm.$onInit = () => initialiseData(vm, initialState);

    vm.$onChanges = () => {
        vm.ratingsByCode = _.keyBy(_.get(vm.ratingScheme, "ratings", []), r => r.rating);
        vm.allocationSchemesById = _.keyBy(vm.allocationSchemes, s => s.id);
    };

    vm.onSelect = (node) => {
        if (vm.selected && vm.selected.rating.id === node.rating.id) {
            selectedMeasurable.set(null);
        } else {
            const selectedNode = Object.assign({}, node, {allocations: enrichAllocationsWithScheme(node, vm.allocationSchemesById)});
            selectedMeasurable.set(selectedNode);
        }
    }

    vm.onToggleView = () => {
        vm.showTreeView = !vm.showTreeView
    }

    selectedMeasurable.subscribe(selected => {
        $scope.$applyAsync(() => {
            vm.selected = selected;
        })
    })
}


controller.$inject = ["$scope"];


const component = {
    template,
    bindings,
    controller
};



export default {
    component,
    id: "waltzMeasurableRatingPanel"
};
