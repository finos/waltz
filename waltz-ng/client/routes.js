/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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


function configureRoutes($locationProvider, $stateProvider, $urlRouterProvider) {

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

    $locationProvider
        .html5Mode(true);
}

configureRoutes.$inject = [
    "$locationProvider",
    "$stateProvider",
    "$urlRouterProvider"
];


// -- SCROLLER ---

function configureScrollToTopOnChange($doc, $transitions) {
    $transitions.onSuccess({}, (transition) => {
        $doc[0].body.scrollTop = 0;
        $doc[0].documentElement.scrollTop = 0;
    });
}


configureScrollToTopOnChange.$inject = [
    "$document",
    "$transitions"
];


// -- NAG ---

function configureBetaNagMessageNotification($transitions,
                                             nagMessageService,
                                             notification) {

    const nagFunction = (message = "") => {
        $transitions.onSuccess({}, (transition) => {
            notification.info(message);
        });
    };

    nagMessageService.setupNag(nagFunction);
}


configureBetaNagMessageNotification.$inject = [
    "$transitions",
    "NagMessageService",
    "Notification"
];


// -- STATE CHANGE ---

function configureStateChangeListener($transitions, $window, accessLogStore) {
    $transitions.onSuccess({}, (transition) => {
        const {name} = transition.to();
        const infoPromise = accessLogStore.write(name, transition.params());

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
    });
}


configureStateChangeListener.$inject = [
    "$transitions",
    "$window",
    "AccessLogStore"
];

// -- ROUTE DEBUGGER ---

function configureRouteDebugging($transitions, $trace) {

    $transitions.onError({}, (transition) => {
        const to = transition.to();
        const from = transition.from();
        const params = transition.params();
        const error = transition.error();
        console.error("Transition Error - fired when an error occurs during transition.", {to, from, params, error});
    });

    // UNCOMMENT FOR FINE GRAINED LOGGING
    // $trace.enable('TRANSITION');
}

configureRouteDebugging.$inject = [
    "$transitions",
    "$trace"
];


// -- INACTIVITY TIMER ---

function configureInactivityTimer($timeout, $transitions, $window, settingsService) {
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

                $transitions.onSuccess({}, (transition) => {
                    //if existing timeout promise, then cancel
                    if (timeoutPromise) {
                        $timeout.cancel(timeoutPromise);
                    };

                    // set a new countdown
                    timeoutPromise = $timeout(reloadPage, inactivityTime);
                });
            }

        });

}


configureInactivityTimer.$inject = [
    "$timeout",
    "$transitions",
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
