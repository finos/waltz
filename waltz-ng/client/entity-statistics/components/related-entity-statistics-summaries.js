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
import {mkChunks} from "../../common/list-utils";
import template from './related-entity-statistics-summaries.html';
import {initialiseData} from "../../common";


const bindings = {
    parentRef: '<',
    definitions: '<',
    summaries: '<'
};


const initialState = {
    chunkedEntries: []
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes => {
        if (vm.summaries) {
            const summariesByDefinitionId = _.keyBy(vm.summaries, 'entityReference.id');
            const entries = _.map(vm.definitions.children,
                c => ({
                    definition: c,
                    summary: summariesByDefinitionId[c.id]
                }));
            vm.chunkedEntries = mkChunks(entries, 2);
        }
    });

}

controller.$inject = [];


const component = {
    template,
    controller,
    bindings
};


export default component;
