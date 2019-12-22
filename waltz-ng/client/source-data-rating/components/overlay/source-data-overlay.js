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
import {initialiseData, isEmpty} from "../../../common/index";
import {CORE_API} from '../../../common/services/core-api-utils';

import template from './source-data-overlay.html';


const bindings = {
    entities: '<',
    visible: '<'
};


const initialState = {
    filteredRatings: [],
    ratings: [],
    entities: []
};


function filterRatings(ratings,
                       entities = []) {
    return isEmpty(entities)
        ? ratings
        : _.filter(ratings, r => _.includes(entities, r.entityKind));
}


function controller(serviceBroker) {

    const vm = initialiseData(this, initialState);


    vm.$onChanges = (changes) => {
        if (vm.visible === true) {
            serviceBroker
                .loadAppData(CORE_API.SourceDataRatingStore.findAll)
                .then(r => {
                    vm.ratings = r.data;
                    vm.filteredRatings = filterRatings(vm.ratings, vm.entities);
                });
        }

    };
}


controller.$inject = ['ServiceBroker'];


const component = {
    bindings,
    controller,
    template
};


export default {
    component,
    id: 'waltzSourceDataOverlay'
}