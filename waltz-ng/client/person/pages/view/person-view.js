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

import {initialiseData} from "../../../common/index";
import {toEntityRef} from "../../../common/entity-utils";
import template from './person-view.html';


const initialState = {
    availableSections: [],
    sections: []
};


function controller(person,
                    dynamicSectionManager,
                    historyStore) {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        if (!person) {
            return;
        }
        vm.person = person;
        vm.entityRef = toEntityRef(person, 'PERSON');
        vm.availableSections = dynamicSectionManager.findAvailableSectionsForKind('PERSON');
        vm.sections = dynamicSectionManager.findUserSectionsForKind('PERSON');
        historyStore.put(
            person.displayName,
            'PERSON',
            'main.person.view',
            { empId: person.employeeId });
    };

    // -- INTERACT --
    vm.addSection = (section) => vm.sections = dynamicSectionManager.openSection(section, 'PERSON');
    vm.removeSection = (section) => vm.sections = dynamicSectionManager.removeSection(section, 'PERSON');
}


controller.$inject = [
    'person',
    'DynamicSectionManager',
    'HistoryStore'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};
