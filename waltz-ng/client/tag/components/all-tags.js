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

import template from "./all-tags.html";
import {CORE_API} from "../../common/services/core-api-utils";

const bindings = {
    targetKind: "@"
};


function controller(serviceBroker, $state) {
    const vm = this;

    vm.$onChanges = () => {
        if(vm.targetKind) {
            serviceBroker
                .loadViewData(
                    CORE_API.TagStore.findTagsByEntityKind,
                    [ vm.targetKind ])
                .then(r => vm.tags = r.data);
        }
    };

    vm.onTagSelect = (tag) => {
        const params = { id: tag.id };
        $state.go(`main.tag.id.${tag.targetKind.toLowerCase()}`, params);
    }
}

controller.$inject = [ "ServiceBroker", "$state" ];

export default {
    template,
    bindings,
    controller
};

