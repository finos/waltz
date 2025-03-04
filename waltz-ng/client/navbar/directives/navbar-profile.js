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

import _ from "lodash";
import {CORE_API, getApiReference} from "../../common/services/core-api-utils";
import template from "./navbar-profile.html";
import roles from "../../user/system-roles";
import ToastStore from "../../svelte-stores/toast-store"
import namedSettings from "../../system/named-settings";

const bindings = {
    logoOverlayText: "<"
};


const initialState = {
    logoOverlayText: "",
    user: null,
    showSysAdminMenuItem: false,
    notificationCountTotal: null,
    notificationsCountsByKind: {},
    roadmapsEnabled: true
};


function loginController($scope, $uibModalInstance, logoOverlayText) {
    $scope.ok = () => {
        const credentials = {
            userName: $scope.username,
            password: $scope.password
        };
        $uibModalInstance.close(credentials);
    };

    $scope.username = "";
    $scope.password = "";
    $scope.logoOverlayText = logoOverlayText || "";

    $scope.cancel = () => $uibModalInstance.dismiss("cancel");
}


loginController.$inject = [
    "$scope",
    "$uibModalInstance",
    "logoOverlayText"
];


function controller($interval,
                    $state,
                    $uibModal,
                    $scope,
                    serviceBroker,
                    settingsService,
                    userService) {
    const vm = _.defaultsDeep(this, initialState);

    settingsService
        .findOrDefault(namedSettings.measurableRatingRoadmapsEnabled, true)
        .then(isEnabled => {
            vm.roadmapsEnabled = !(isEnabled === 'false');
        });

    settingsService
        .findOrDefault("web.authentication", "")
        .then(webAuthentication => {
            vm.allowDirectLogin = webAuthentication === "waltz";
        });

    userService
        .whoami(true) // force
        .then(user => vm.user = user)
        .then(() => vm.showSysAdminMenuItem = userService.hasRole(vm.user, roles.ADMIN)
                                                || userService.hasRole(vm.user, roles.USER_ADMIN)
                                                || userService.hasRole(vm.user, roles.LICENCE_ADMIN));

    const notificationCacheRefreshListener = (e) => {
        if (e.eventType === "REFRESH"
            && getApiReference(e.serviceName, e.serviceFnName) === CORE_API.NotificationStore.findAll) {
            loadNotifications();
        }
    };

    const loadNotifications = () => {
        return serviceBroker
            .loadAppData(CORE_API.NotificationStore.findAll, [], {
                cacheRefreshListener: {
                    componentId: "waltzNavbarProfile",
                    fn: notificationCacheRefreshListener
                }
            })
            .then(r => {
                const notificationSummaries = r.data.summary;
                vm.notificationCountTotal = _.sumBy(notificationSummaries, "count");
                vm.notificationsCountsByKind = _.keyBy(notificationSummaries, "kind");
                $scope.notificationMessage = r.data.message;
            });
    };

    const setupNotificationTimer = () => {
        const fn = () => serviceBroker.loadAppData(CORE_API.NotificationStore.findAll, [], { force: true });
        $interval(fn, 300000);
    };

    loadNotifications()
        .then(() => setupNotificationTimer());


    $scope
        .$watch("notificationMessage", function (newValue) {
            if(!_.isEmpty(newValue)){
                ToastStore.confirmInfo(newValue);
            }
        });

    const reloadPage = () => $state.reload();

    const rejected = () => alert("Invalid username/password");

    const logout = () => userService
        .logout()
        .then(reloadPage);


    vm.logout = logout;
    vm.login = () => {

        var loginModalInstance = $uibModal.open({
            animation: true,
            templateUrl: "navbar/modal-login.html",
            controller: loginController,
            resolve: {
                logoOverlayText: () => vm.logoOverlayText
            },
            size: "sm"
        });

        loginModalInstance.result
            .then(
                (credentials) => userService
                    .login(credentials)
                    .then(reloadPage, rejected),
                () => console.log("Login dismissed at: " + new Date()));

    };

}


controller.$inject = [
    "$interval",
    "$state",
    "$uibModal",
    "$scope",
    "ServiceBroker",
    "SettingsService",
    "UserService"
];


const directive = {
    restrict: "E",
    replace: true,
    scope: {},
    bindToController: bindings,
    controllerAs: "ctrl",
    controller,
    template
};


export default () => directive;