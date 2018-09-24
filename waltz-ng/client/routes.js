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

import {CORE_API} from "./common/services/core-api-utils";
import WelcomeJs from "./welcome/welcome.js";
import WelcomeHtml from "./welcome/welcome.html";


function warmUpCache($q, serviceBroker) {
    const promises = $q.all([
        serviceBroker
            .loadAppData(CORE_API.EnumValueStore.findAll),
        serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll),
        serviceBroker
            .loadAppData(CORE_API.DrillGridDefinitionStore.findAll)
    ]);
    return promises;
}


warmUpCache.$inject = [
    "$q",
    "ServiceBroker"
];


function configureRoutes($stateProvider, $urlRouterProvider) {

    $urlRouterProvider.when("", "/home");

    $stateProvider
        .state("main", {
            url: "/",
            resolve: {
                warmUp: warmUpCache
            },
            views: {
                "header": {template: "<waltz-navbar></waltz-navbar>"},
                "content": WelcomeJs
            }
        })
        .state("main.home", {
            url: "home",
            views: {
                "content": {template: WelcomeHtml}
            }
        });
}

configureRoutes.$inject = [
    "$stateProvider",
    "$urlRouterProvider"
];


// -- SCROLLER ---

function configureScrollToTopOnChange($rootScope, $doc) {
    $rootScope.$on("$stateChangeSuccess", () => {
        $doc[0].body.scrollTop = 0;
        $doc[0].documentElement.scrollTop = 0;
    });
}


configureScrollToTopOnChange.$inject = [
    "$rootScope",
    "$document"
];


// -- NAG ---

function configureBetaNagMessageNotification($rootScope,
                                             nagMessageService,
                                             notification) {

    const nagFunction = (message = "") => {
        $rootScope.$on("$stateChangeSuccess", () => {
            notification.info(message);
        });
    };

    nagMessageService.setupNag(nagFunction);
}


configureBetaNagMessageNotification.$inject = [
    "$rootScope",
    "NagMessageService",
    "Notification"
];


// -- STATE CHANGE ---

function configureStateChangeListener($rootScope, $window, accessLogStore) {
    $rootScope.$on(
        "$stateChangeSuccess",
        (event, toState, toParams /* fromState, fromParams */ ) => {
            const {name} = toState;
            const infoPromise = accessLogStore.write(name, toParams);

            if (__ENV__ === "prod") {
                infoPromise.then(info => {
                    if (info.revision != __REVISION__) {
                        console.log(
                            "Waltz reloading as server reported version does not match client. Server:",
                            info,
                            "client: ",
                            __REVISION__);
                        $window.location.reload()
                    }
                })

            }
        }
    );
}


configureStateChangeListener.$inject = [
    "$rootScope",
    "$window",
    "AccessLogStore"
];

// -- ROUTE DEBUGGER ---

function configureRouteDebugging($rootScope) {

    $rootScope.$on("$stateChangeError",function(event, toState, toParams, fromState, fromParams, error){
        console.log("$stateChangeError - fired when an error occurs during transition.");
        console.log(arguments);
    });

    $rootScope.$on("$stateNotFound",function(event, unfoundState, fromState, fromParams){
        console.log(`$stateNotFound ${unfoundState.to} - fired when a state cannot be found by its name.`);
        console.log(unfoundState, fromState, fromParams);
    });

    // UNCOMMENT FOR FINE GRAINED LOGGING
    //
    // $rootScope.$on("$stateChangeStart",function(event, toState, toParams, fromState, fromParams){
    //     console.log(`$stateChangeStart to ${toState.name} - fired when the transition begins.`, {toState, toParams} );
    // });
    // $rootScope.$on("$stateChangeSuccess",function(event, toState, toParams, fromState, fromParams){
    //     console.log(`$stateChangeSuccess to ${toState.name} - fired once the state transition is complete.`);
    // });
    // $rootScope.$on("$viewContentLoading",function(event, viewConfig){
    //     console.log("$viewContentLoading - view begins loading - dom not rendered", viewConfig);
    // });

}

configureRouteDebugging.$inject = ["$rootScope"];


// -- INACTIVITY TIMER ---

function configureInactivityTimer($rootScope, $timeout, $window, settingsService) {
    const uiInactivityTimeoutSettingKey = "ui.inactivity-timeout";

    const reloadPage = () => {
        console.log("Reloading page due to inactivity");
        $window.location.reload();
    };

    settingsService
        .findOrDefault(uiInactivityTimeoutSettingKey, null)
        .then(inactivityTime  => {

            if(inactivityTime) {
                console.log("Configuring inactivity timer for " + inactivityTime + " ms");
                let timeoutPromise = $timeout(reloadPage, inactivityTime);

                $rootScope.$on(
                    "$stateChangeSuccess",
                    (event, toState, toParams /* fromState, fromParams */) => {

                        //if existing timeout promise, then cancel
                        if (timeoutPromise) {
                            $timeout.cancel(timeoutPromise);
                        };

                        // set a new countdown
                        timeoutPromise = $timeout(reloadPage, inactivityTime);
                    }
                );
            }

        });

}


configureInactivityTimer.$inject = [
    "$rootScope",
    "$timeout",
    "$window",
    "SettingsService"
];


// -- SETUP ---

function setup(module) {
    module
        .config(configureRoutes)
        .run(configureScrollToTopOnChange)
        .run(configureBetaNagMessageNotification)
        .run(configureStateChangeListener)
        .run(configureInactivityTimer)
        .run(configureRouteDebugging);

}


export default setup;
