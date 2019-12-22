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
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import template from "./entity-enum.html";
import {mkChunks} from "../../../common/list-utils";


const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    numColumns: 2,
    chunkedEntries: []
};


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        if (vm.parentEntityRef) {
            const definitionsPromise = serviceBroker
                .loadViewData(
                    CORE_API.EntityEnumStore.findDefinitionsByEntityKind,
                    [vm.parentEntityRef.kind])
                .then(result => result.data);

            const valuesPromise = serviceBroker
                .loadViewData(
                    CORE_API.EntityEnumStore.findValuesByEntity,
                    [vm.parentEntityRef])
                .then(result => result.data);

            $q.all([definitionsPromise, valuesPromise])
                .then(([defs, vals]) => {
                    const valsByDefId = _.keyBy(vals, "definitionId");

                    const entries = _.chain(defs)
                        .map(d => ({
                            definition: d,
                            value: valsByDefId[d.id] || {enumValueKey: "-"}
                        }))
                        .sortBy(["definition.position", "definition.id"])
                        .value();

                    vm.chunkedEntries = mkChunks(entries, vm.numColumns);
                });

        }
    };
}


controller.$inject = [
    "$q",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzEntityEnum"
};
