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


function controller(settingsService, $timeout) {

    const vm = initialiseData(this, initialState);

    const removeIssueNotificationMsg = () => {
        console.log("Removing issue notification message.");
        vm.bannerVisible = false;
    };

    const getIssueNotificationMsg = () => {
        settingsService
        .findOrDefault("waltz.issue.notification.message", null)
        .then(setting => vm.bannerMessage = setting);

    };

    settingsService
        .findOrDefault("waltz.issue.notification.time", 0)
        .then(notificationTimeout  => {
            if(+notificationTimeout > 0) {
                getIssueNotificationMsg();
                console.log("Configuring issue notification time and message for " + notificationTimeout + " ms");
                vm.bannerVisible = true;
                $timeout(removeIssueNotificationMsg, notificationTimeout);

            } else if(notificationTimeout === '' || notificationTimeout === null) {
                getIssueNotificationMsg();
                console.log("Configuring issue notification time and message");
                console.log("Admin will remove the notification once issue is resolved.");
                vm.bannerVisible = true;
            } else if(+notificationTimeout === 0){
                vm.bannerVisible = false;
            }

        });  
}


controller.$inject = [
    "SettingsService",
    "$timeout"
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