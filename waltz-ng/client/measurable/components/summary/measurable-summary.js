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
import {enrichServerStats} from "../../../server-info/services/server-utilities";
import {calcComplexitySummary} from "../../../complexity/services/complexity-utilities";
import {CORE_API} from "../../../common/services/core-api-utils";
import {entity} from "../../../common/services/enums/entity";
import {getEnumName} from "../../../common/services/enums";
import template from "./measurable-summary.html";
import {initialiseData} from "../../../common/index";


const bindings = {
    parentEntityRef: "<",
    scope: "@?",
    applications: "<",
    children: "<",
    measurable: "<",
    parents: "<",
    serverStats: "<"
};


const initialState = {
    scope: "CHILDREN"
};


function enrichWithRefs(measurables = []) {
    return _.map(
        measurables,
        d => Object.assign(
            {},
            d,
            { entityReference: {kind: "MEASURABLE", id: d.id, name: d.name, description: d.description }}));
}


function prepareChildRefs(children = [], measurable = {}) {
    const cs = _.chain(children)
        .filter(c => c.id !== measurable.id)  // not self
        .filter(c => c.parentId === measurable.id) // only immediate children
        .value();
    return enrichWithRefs(cs);
}


function prepareParentRefs(parents = [], measurable = {}) {
    const ps = _.filter(parents, p => p.id !== measurable.id); // not self
    return enrichWithRefs(ps);
}


function prepareRelationshipStats(stats = []) {
    return _
        .chain(stats)
        .map((v,k) => { return { kind: k, name: getEnumName(entity, k), count: v }})
        .orderBy("name")
        .value();
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const selector = {
            entityReference: vm.parentEntityRef,
            scope: vm.scope
        };

        serviceBroker
            .loadViewData(
                CORE_API.AssetCostStore.findTotalCostForAppSelector,
                [selector])
            .then(r => vm.totalCost = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.ComplexityStore.findBySelector,
                [ selector ])
            .then(r => vm.complexitySummary = calcComplexitySummary(r.data));
    };

    vm.$onChanges = (c) => {
        if (c.serverStats) vm.enrichedServerStats = enrichServerStats(vm.serverStats);
        if (c.children || c.measurable) vm.childRefs = prepareChildRefs(vm.children, vm.measurable);
        if (c.parents || c.measurable) vm.parentRefs = prepareParentRefs(vm.parents, vm.measurable);

        if (vm.measurable) {
            vm.entityRef = {
                kind: "MEASURABLE",
                id: vm.measurable.id
            };

            serviceBroker
                .loadViewData(
                    CORE_API.MeasurableRelationshipStore.tallyByEntityReference,
                    [ { id: vm.measurable.id, kind: "MEASURABLE" }])
                .then(r => vm.relationshipStats = prepareRelationshipStats(r.data));
        }
    };
}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzMeasurableSummary"
};