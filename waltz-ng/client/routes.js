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

import {CORE_API} from "./common/services/core-api-utils";
import WelcomeJs from "./welcome/welcome.js";
import {activeSections} from "./dynamic-section/section-store";
import toasts from "./svelte-stores/toast-store";
import popover from "./svelte-stores/popover-store";


function warmUpCache($q, serviceBroker) {
    return $q
        .all([
            serviceBroker
                .loadAppData(CORE_API.EnumValueStore.findAll),
            serviceBroker
                .loadAppData(CORE_API.DataTypeStore.findAll),
        ]);
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
                "header@": {template: "<waltz-navbar></waltz-navbar>"},
                "content@": WelcomeJs
            },
        })
        .state("main.home", {
            url: "home",
            views: {
                "content@": WelcomeJs
            },
            onEnter: () => activeSections.setPageKind("HOME")
        })

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
    $transitions.onSuccess({}, () => {
        $doc[0].body.scrollTop = 0;
        $doc[0].documentElement.scrollTop = 0;
        // remove any popovers, as unlikely they would still be wanted after a page transition
        popover.dismiss();
    });
}


configureScrollToTopOnChange.$inject = [
    "$document",
    "$transitions"
];


// -- NAG ---

function configureBetaNagMessageNotification($transitions,
                                             nagMessageService) {

    const nagFunction = (message = "") => {
        $transitions.onSuccess({}, () => {
            toasts.info(message);
        });
    };

    nagMessageService.setupNag(nagFunction);
}


configureBetaNagMessageNotification.$inject = [
    "$transitions",
    "NagMessageService"
];


// -- STATE CHANGE ---

function configureStateChangeListener($transitions, $window, $state, serviceBroker) {
    $transitions.onExit({}, () => activeSections.exitPage());
    $transitions.onSuccess({}, d => activeSections.setPageKind(d.targetState().name()));

    activeSections.subscribe((d) => {

        const newSections = _.difference(
            _.map(d.sections, s => s.componentId),
            _.map(d.previous, s => s.componentId));

        const params = $state.params;

        newSections
            .forEach(s => serviceBroker.execute(
                CORE_API.AccessLogStore.write,
                [`${d.pageKind}|${s}`, params]));
    });

    $transitions.onSuccess({}, (transition) => {
        const {name} = transition.to();

        const infoPromise = serviceBroker
            .execute(CORE_API.AccessLogStore.write, [name, transition.params()]);

        if (__ENV__ === "prod") {
            infoPromise.then(r => {
                if (r.data.revision !== __REVISION__) {
                    console.log(
                        "Waltz reloading as server reported version does not match client. Server:",
                        r,
                        "client: ",
                        __REVISION__);
                    $window.location.reload();
                }
            })
        }
    });
}


configureStateChangeListener.$inject = [
    "$transitions",
    "$window",
    "$state",
    "ServiceBroker"
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
    //$trace.enable('TRANSITION');
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

                $transitions.onSuccess({}, () => {
                    //if existing timeout promise, then cancel
                    if (timeoutPromise) {
                        $timeout.cancel(timeoutPromise);
                    }

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
