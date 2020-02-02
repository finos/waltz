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
import HomePage from "./pages/home/person-home";
import PersonPage from "./pages/view/person-view";
import {CORE_API} from "../common/services/core-api-utils";


function personEmployeeIdBouncer($state, $stateParams, serviceBroker) {
    const empId = $stateParams.empId;
    serviceBroker
        .loadViewData(
            CORE_API.PersonStore.getByEmployeeId,
            [ empId ])
        .then(r => r.data)
        .then(person => {
            if (person) {
                return $state.go("main.person.id", {id: person.id}, { location: "replace"});
            } else {
                console.log(`Cannot find corresponding person: ${empId}`);
            }
        });
}


personEmployeeIdBouncer.$inject = [
    "$state",
    "$stateParams",
    "ServiceBroker"
];


function userIdBouncer($state, $stateParams, serviceBroker) {
    const userId = $stateParams.userId;
    serviceBroker
        .loadViewData(
            CORE_API.PersonStore.findByUserId,
            [ userId ])
        .then(r => r.data)
        .then(person => {
            if (person) {
                return $state.go("main.person.id", {id: person.id}, { location: "replace"});
            } else {
                console.log(`Cannot find corresponding person: ${userId}`);
            }
        });
}


userIdBouncer.$inject = [
    "$state",
    "$stateParams",
    "ServiceBroker"
];


// --- ROUTES ---

const personHome = {
    url: "person",
    views: {"content@": HomePage }
};

const personViewByEmployeeId = {
    url: "/{empId:string}",
    views: {"content@": PersonPage },
    resolve: {
        bouncer: personEmployeeIdBouncer
    }
};

const personViewByUserId = {
    url: "/user-id/{userId:string}",
    views: {"content@": PersonPage },
    resolve: {
        bouncer: userIdBouncer
    }
};

const personViewByPersonId = {
    url: "/id/{id:int}",
    views: {"content@": PersonPage },
};


// --- SETUP ---

function setup($stateProvider) {
    $stateProvider
        .state("main.person", personHome)
        .state("main.person.view", personViewByEmployeeId)
        .state("main.person.id", personViewByPersonId)
        .state("main.person.userId", personViewByUserId);
}

setup.$inject = ["$stateProvider"];


export default setup;
