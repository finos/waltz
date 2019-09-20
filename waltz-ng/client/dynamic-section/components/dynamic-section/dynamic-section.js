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

import template from "./dynamic-section.html";
import {initialiseData} from "../../../common/index";
import {kindToViewState} from "../../../common/link-utils";


const bindings = {
    parentEntityRef: "<",
    section: "<",
    onRemove: "<",
};

const initialState = {
    embedded: false,
    backLink: {
        state: "",
        params: {}
    }
};


function controller($state) {
    const vm = initialiseData(this, initialState);
    vm.embedded = _.startsWith($state.current.name, "embed");

    vm.$onChanges = () => {
        if (vm.parentEntityRef !== null) {
            vm.backLink = {
                state: kindToViewState(vm.parentEntityRef.kind),
                params: { id: vm.parentEntityRef.id },
            };
        }
    };

}


controller.$inject = [
    "$state"
];


const component = {
    controller,
    template,
    bindings,
    transclude: true
};

const id = "waltzDynamicSection";


export default {
    id,
    component
};