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

import template from "./scenario-view.html";
import {initialiseData} from "../../../common";


const bindings = {
};


const initialState = {
};


function controller($stateParams, dynamicSectionManager) {
    const vm = initialiseData(this, initialState);

    dynamicSectionManager.initialise("SCENARIO");

    vm.$onInit = () => {
        console.log("onInit");
        vm.scenarioId = $stateParams.id;
        vm.parentEntityRef = {
            kind: "SCENARIO",
            id: vm.scenarioId
        };

    };

}


controller.$inject = [
    "$stateParams",
    "DynamicSectionManager"
];


const component = {
    bindings,
    controller,
    template,
};


export default {
    id: "waltzScenarioView",
    component
};



