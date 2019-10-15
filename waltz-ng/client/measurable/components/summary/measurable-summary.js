/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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
import {
    findNode,
    flattenChildren,
    getParents,
    populateParents
} from "../../../common/hierarchy-utils";
import {kindToViewState} from "../../../common/link-utils";
import {entityLifecycleStatus as EntityLifecycleStatus} from "../../../common/services/enums/entity-lifecycle-status";
import {mkApplicationSelectionOptions, mkSelectionOptions} from "../../../common/selector-utils";


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

        serviceBroker
            .loadViewData(
                CORE_API.MeasurableStore.getById,
                [ vm.parentEntityRef.id  ])
            .then(r => vm.measurable = r.data)
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
                        })
                }
            });

        serviceBroker
            .loadViewData(
                CORE_API.ApplicationStore.findBySelector,
                [selector])
            .then(r => vm.applications = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.TechnologyStatisticsService.findBySelector,
                [selector])
            .then(r => {
                vm.techStats = r.data;
                vm.enrichedServerStats = enrichServerStats(vm.techStats.serverStats)
            });

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
    id: "waltzMeasurableSummary"
};