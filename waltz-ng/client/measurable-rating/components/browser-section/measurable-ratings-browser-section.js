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

import {initialiseData} from "../../../common";
import template from "./measurable-ratings-browser-section.html";
import {CORE_API} from "../../../common/services/core-api-utils";

/**
 * @name waltz-measurable-ratings-browser
 *
 * @description
 * This component ...
 */


const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    visibility: {
        treeView: true,
        gridView: false,
        gridAvailable: false,
    }
};



function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.DrillGridDefinitionStore.findAll)
            .then(r => vm.visibility.gridAvailable = r.data.length > 0);

        serviceBroker
            .loadViewData(
                CORE_API.RoadmapStore.findRoadmapsAndScenariosByFormalRelationship,
                [ vm.parentEntityRef ])
            .then(r => vm.roadmapReferences = r.data);
    };

    vm.showGridView = () => {
        vm.visibility.gridView = true;
        vm.visibility.treeView = false;
    };

    vm.showTreeView = () => {
        vm.visibility.gridView = false;
        vm.visibility.treeView = true;
    };
}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzMeasurableRatingsBrowserSection"
};