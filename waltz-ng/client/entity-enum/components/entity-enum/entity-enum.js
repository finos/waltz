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

import _ from "lodash";
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import template from "./entity-enum.html";


const bindings = {
    parentEntityRef: '<'
};


const initialState = {
    numColumns: 2,
    chunkedEntries: []
};


function mkChunks(entries, chunkSize) {
    const chunkedEntries = [];
    for (var i = 0; i < entries.length; i += chunkSize) {
        chunkedEntries.push(entries.slice(i, i + chunkSize));
    }
    return chunkedEntries;
}


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
                    const valsByDefId = _.keyBy(vals, 'definitionId');

                    const entries = _.chain(defs)
                        .map(d => ({
                            definition: d,
                            value: valsByDefId[d.id] || {enumValueKey: "-"}
                        }))
                        .sortBy(o => [o.definition.position, o.definition.id])
                        .value();

                    vm.chunkedEntries = mkChunks(entries, vm.numColumns);
            });

        }
    };
}


controller.$inject = [
    '$q',
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzEntityEnum'
};
