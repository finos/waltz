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
import template from './bookmark-kind-select.html'
import {CORE_API} from "../../../common/services/core-api-utils";


const bindings = {
    selected: '<',
    disabledCondition: '<',
    onSelect: '&'
};


function createKinds(serviceBroker) {
    return serviceBroker
        .loadAppData(CORE_API.EnumValueStore.findAll)
        .then(r => {
            return _
                .chain(r.data)
                .filter(d => d.type === 'BookmarkKind')
                .sortBy('name')
                .map(d => ({
                    code: d.key,
                    name: d.name,
                    icon: d.icon
                }))
                .value();
        });
}

function controller(serviceBroker) {

    const vm = this;

    vm.$onInit = () => {
        createKinds(serviceBroker)
            .then(kinds => vm.kinds = kinds);
    };

    vm.onChange = (kind) => {
        if (!vm.onSelect()) {
            console.warn('bookmark-kind-select: No handler for change notification is registered.  Please provide an on-select callback')
            return;
        }
        vm.onSelect()(kind.code, kind);
    };
}


controller.$inject = ['ServiceBroker'];


const component = {
    template,
    controller,
    bindings
};


export default {
    component,
    id: 'waltzBookmarkKindSelect'
};

