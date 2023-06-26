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
import {CORE_API} from "../../../common/services/core-api-utils";
import {entity} from "../../../common/services/enums/entity";
import {getEnumName} from "../../../common/services/enums";
import template from "./measurable-overview.html";
import {initialiseData} from "../../../common/index";
import {findNode, flattenChildren, getParents, populateParents} from "../../../common/hierarchy-utils";
import {kindToViewState} from "../../../common/link-utils";
import {entityLifecycleStatus as EntityLifecycleStatus} from "../../../common/services/enums/entity-lifecycle-status";
import {mkSelectionOptions} from "../../../common/selector-utils";


const bindings = {
    filters: "<",
    parentEntityRef: "<"
};


const initialState = {
    applications: [],
    techStats: [],
    hierarchyMode: "RELEVANT"
};


function prepareRelationshipStats(stats = []) {
    return _
        .chain(stats)
        .map((v,k) => { return { kind: k, name: getEnumName(entity, k), count: v }})
        .orderBy("name")
        .value();
}


function controller(serviceBroker, $state) {
    const vm = initialiseData(this, initialState);

    const loadAll = () => {

        const selector = mkSelectionOptions(
            vm.parentEntityRef,
            undefined,
            undefined,
            vm.filters);

        const measurablePromise = serviceBroker
            .loadViewData(
                CORE_API.MeasurableStore.getById,
                [ vm.parentEntityRef.id  ])
            .then(r => vm.measurable = r.data);

        measurablePromise
            .then(() => {
                if (vm.measurable.entityLifecycleStatus !== EntityLifecycleStatus.REMOVED.key) {
                    serviceBroker
                        .loadAppData(
                            CORE_API.MeasurableStore.findAll)
                        .then(r => {
                            const all = _.filter(r.data, m => m.categoryId === vm.measurable.categoryId);
                            const roots = populateParents(all, true);
                            const m = findNode(roots, vm.measurable.id);
                            const parents = getParents(m);
                            const children = flattenChildren(m);
                            const limb = _.union(parents, [m], children);
                            const relevantHierarchy = _.map(limb, n => {
                                const cpy = Object.assign({}, n, {parent: n.parent ? n.parent.id : null});
                                delete cpy.children;
                                return cpy;
                            });
                            vm.entireHierarchy = all;
                            vm.relevantHierarchy = relevantHierarchy;
                            vm.hierarchy = vm.relevantHierarchy;
                            vm.expandedNodes = parents;
                        });
                }
            });

        measurablePromise
            .then(() => vm.measurable.organisationalUnitId)
            .then(ouId => _.isNil(ouId)
                ? Promise.resolve(null)
                : serviceBroker
                    .loadViewData(CORE_API.OrgUnitStore.getById, [ouId])
                    .then(r => vm.owningOrgUnit = r.data));

        serviceBroker
            .loadViewData(
                CORE_API.ApplicationStore.findBySelector,
                [selector])
            .then(r => vm.applications = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.MeasurableRelationshipStore.tallyByEntityReference,
                [ vm.parentEntityRef ])
            .then(r => vm.relationshipStats = prepareRelationshipStats(r.data));
    };


    vm.$onInit = () => {
        loadAll();
    };


    vm.$onChanges = () => {
        loadAll();
    };


    vm.onSelectNavItem = (item) => {
        if (item.id === vm.parentEntityRef.id) {
            return; // nothing to do, user clicked on self
        }
        $state.go(
            kindToViewState(entity.MEASURABLE.key),
            { id: item.id });
    };


    vm.onToggleHierarchyMode = () => {
        if (vm.hierarchyMode === "ENTIRE") {
            vm.hierarchyMode = "RELEVANT";
            vm.hierarchy = vm.relevantHierarchy;
        } else {
            vm.hierarchyMode = "ENTIRE";
            vm.hierarchy = vm.entireHierarchy;
        }
    };

}


controller.$inject = [
    "ServiceBroker",
    "$state"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzMeasurableOverview"
};