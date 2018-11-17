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
import template from "./measurable-category-edit.html";
import {toEntityRef} from "../../../common/entity-utils";


const modes = {
    TREE_VIEW : "TREE_VIEW",
    NODE_VIEW: "NODE_VIEW",
    CHANGE_VIEW: "CHANGE_VIEW"
};


const initialState = {
    category: null,
    measurables: [],
    selectedMeasurable: null,
    selectedChange: null,
    recentlySelected: [],
    pendingChanges: [],
    mode: modes.TREE_VIEW
};


function controller($q,
                    $state,
                    $stateParams,
                    serviceBroker) {

    const vm = initialiseData(this, initialState);
    const categoryId = $stateParams.id;

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll)
            .then(r => vm.measurables = _.filter(r.data, m => m.categoryId === categoryId));

        serviceBroker
            .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
            .then(r => vm.category = _.find(r.data, { id: categoryId }))
            .then(() => serviceBroker.loadViewData(
                    CORE_API.TaxonomyManagementStore.findPendingChangesByDomain,
                    [ toEntityRef(vm.category) ]))
            .then(r => vm.pendingChanges = console.log(r.data) || r.data);
    };

    const clearSelections = () => {
        vm.selectedMeasurable = null;
        vm.selectedChange = null;
    };

    vm.onSelect = (d) => {
        clearSelections();
        vm.mode = modes.NODE_VIEW;
        vm.recentlySelected = _.unionBy(vm.recentlySelected, [d], d => d.id);
        vm.selectedMeasurable = d;
    };

    vm.onDismissSelection = () => {
        clearSelections();
        vm.mode = modes.TREE_VIEW;
    };



}


controller.$inject = [
    "$q",
    "$state",
    "$stateParams",
    "ServiceBroker"
];


export default {
    template,
    controller,
    controllerAs: "$ctrl"
};
