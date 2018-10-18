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

import template from "./data-type-view.html";
import {toEntityRef} from "../../../common/entity-utils";


const initialState = {
    dataType: null,
    entityRef: null,
    sections: [],
    availableSections: []
};


function controller(dataType,
                    dynamicSectionManager,
                    historyStore) {

    const vm = initialiseData(this, initialState);

    if(dataType) {
        vm.entityRef = toEntityRef(dataType, "DATA_TYPE");
        vm.dataType = dataType;

        // -- BOOT ---
        vm.$onInit = () => {
            vm.availableSections = dynamicSectionManager.findAvailableSectionsForKind("DATA_TYPE");
            vm.sections = dynamicSectionManager.findUserSectionsForKind("DATA_TYPE");
            historyStore.put(
                dataType.name,
                "DATA_TYPE",
                "main.data-type.view",
                {id: dataType.id});
        };
    }
    // -- INTERACT --
    vm.addSection = (section) => vm.sections = dynamicSectionManager.openSection(section, "DATA_TYPE");
    vm.removeSection = (section) => vm.sections = dynamicSectionManager.removeSection(section, "DATA_TYPE");
}


controller.$inject = [
    "dataType",
    "DynamicSectionManager",
    "HistoryStore"
];


export default {
    template,
    controller,
    controllerAs: "ctrl"
};