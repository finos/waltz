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

import template from "./issue-notification-banner.html";
import _ from "lodash";

const bindings = {
};


const initialState = {
    bannerVisible: false,
    bannerMessage: null
};


function controller($rootScope) {

    const vm = initialiseData(this, initialState);

    $rootScope.$on('notificationMessageChanged', function (event, value) {
        if(!_.isEmpty(value)){
            vm.bannerMessage = value;
            vm.bannerVisible = true;
        }else{
            vm.bannerMessage = null;
            vm.bannerVisible = false;
        }
        
    });
   
    vm.closeBanner = () => {
        vm.bannerVisible = false;
    }        
}


controller.$inject = [
    "$rootScope"
];


const component = {
    bindings,
    controller,
    template
};


export default {
    component,
    id: "waltzIssueNotificationBanner"
};