/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
import {baseRagNames} from "../../rating-utils";
import _ from "lodash";


const bindings = {
    selected: '<',
    editDisabled: '<',
    onSelect: '<',
    onKeypress: '<',
    ragNames: '<',
    hideOptions: '<'
};


const template = require('./rating-picker.html');


const initialState = {
    pickerStyle: {},
    hideOptions: [],
    onSelect: (rating) => 'No onSelect handler defined for rating-picker: ' + rating,
    ragNames: baseRagNames
};


function controller() {
    const vm = this;

    vm.$onInit = () => initialiseData(this, initialState);

    vm.$onChanges = (c) => {
        if (c.disabled) {
            vm.pickerStyle = vm.disabled
                ? { opacity: 0.4 }
                : [];
        }

        if (c.ragNames) {
            vm.options = _
                .chain(vm.ragNames)
                .map((v, k) => {
                    return {
                        value: k,
                        clazz: `rating-${k}`,
                        label: v
                    };
                })
                .reject(option => _.includes(vm.hideOptions, option.value))
                .value();
        }
    }

}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;