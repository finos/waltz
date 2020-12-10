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

import {CORE_API} from "../common/services/core-api-utils";
import LogicalDataElementView from './logical-data-element-view';

const baseState = {
};


const viewState = {
    url: 'logical-data-element/{id:int}',
    views: {'content@': LogicalDataElementView },
};


function bouncer($state, $stateParams, serviceBroker) {
    const externalId = $stateParams.externalId;
    serviceBroker
        .loadViewData(CORE_API.LogicalDataElementStore.getByExternalId, [externalId])
        .then(r => {
            const element = r.data;
            if(element) {
                $state.go('main.logical-data-element.view', {id: element.id});
            } else {
                console.log(`Cannot find logical data element corresponding to external id: ${externalId}`);
            }
        });
}

bouncer.$inject = ['$state', '$stateParams', 'ServiceBroker'];


const bouncerState = {
    url: 'logical-data-element/external-id/{externalId:string}',
    resolve: {
        bouncer
    }
};

function setup($stateProvider) {

    $stateProvider
        .state('main.logical-data-element.bouncer', bouncerState)
        .state('main.logical-data-element', baseState)
        .state('main.logical-data-element.view', viewState);
}


setup.$inject = ['$stateProvider'];


export default setup;