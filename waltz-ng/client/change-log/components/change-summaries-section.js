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

import {initialiseData} from "../../common";
import template from './change-summaries-section.html';


const bindings = {
};


const initialState = {
    selectedDate: null,
    favouritesGroup: {id: 11874}
};




function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        console.log(vm.selectedDate);
        console.log(vm.selectedDate);

        // serviceBroker
        //     .loadViewData(CORE_API.AppGroupStore.findMyGroupSubscriptions, [])
        //     .then(r => vm.favouritesGroup = r.data)
    };


    vm.onSelectDate = (date) => {
        vm.selectedDate = date;
    }
}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    bindings,
    template,
    controller
};


export default {
    id: "waltzChangeSummariesSection",
    component
}