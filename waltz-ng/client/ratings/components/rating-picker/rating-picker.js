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
import {initialiseData} from "../../../common";
import _ from "lodash";
import {determineForegroundColor} from "../../../common/colors";
import template from "./rating-picker.html";


const bindings = {
    selected: "<", // code of selected item
    editDisabled: "<",
    onSelect: "<?",
    onKeypress: "<?",
    ratingSchemeItems: "<",
};


const initialState = {
    pickerStyle: {},
    ratingSchemeItems: [],
    onSelect: (rating) => "No onSelect handler defined for rating-picker: " + rating,
};


function controller() {
    const vm = this;

    vm.$onInit = () => initialiseData(this, initialState);

    vm.$onChanges = (c) => {
        if (c.ratingSchemeItems && vm.ratingSchemeItems) {
            vm.options = _
                .chain(vm.ratingSchemeItems)
                .filter(d => d.userSelectable)
                .map(d => Object.assign({}, d, { foregroundColor: determineForegroundColor(d.color) }))
                .orderBy(d => d.position)
                .value();
        }
        if (c.editDisabled) {
            vm.pickerStyle = vm.editDisabled
                ? { opacity: 0.4 }
                : [];
        }

    }

}


const component = {
    bindings,
    template,
    controller
};


export default {
    id: "waltzRatingPicker",
    component
};