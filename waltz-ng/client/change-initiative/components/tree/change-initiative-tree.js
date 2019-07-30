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

import template from "./change-initiative-tree.html";
import { initialiseData } from "../../../common";
import { CORE_API } from "../../../common/services/core-api-utils";
import { sameRef } from "../../../common/entity-utils";
import { mkSelectionOptions } from "../../../common/selector-utils";
import {buildHierarchies, findNode, getParents, switchToParentIds} from "../../../common/hierarchy-utils";
import { kindToViewState } from "../../../common/link-utils";
import { entity } from "../../../common/services/enums/entity";
import { fakeParentsByChildKind } from "../../change-initiative-utils";

const bindings = {
    parentEntityRef: "<",
    displayRetired: "<"
};

const initialState = {};


function mkTreeData(changeInitiatives = [], parentEntityRef, displayRetired = true) {
    const initiatives = _
        .chain(changeInitiatives)
        .flatMap(d => {
            const maybeFakeParent = d.parentId
                ? null // fake parent not needed
                : fakeParentsByChildKind[d.changeInitiativeKind]; // use fake parent for kind (except for top level items)

            const enriched = Object.assign(
                {},
                d,
                {
                    parentId: d.parentId || _.get(maybeFakeParent, "id", null),
                    isSelf: sameRef(d, parentEntityRef, { skipChecks: true })
                });
            return [enriched, maybeFakeParent]

        })
        .compact()
        .uniqBy(d => d.id)
        .filter(d => displayRetired
            ? true
            : d.lifecyclePhase !== "RETIRED")
        .value();

    const hierarchy = buildHierarchies(initiatives, false);

    const self = findNode(hierarchy, parentEntityRef.id);
    const byId = _.keyBy(initiatives, d => d.id);

    const expandedNodes =  _.concat(
        [self],
        getParents(self, n => byId[n.parentId]));

    return {
        hierarchy: switchToParentIds(hierarchy),
        initiatives,
        expandedNodes
    };
}


function expandNodes(hierarchy, initiatives, parentEntityRef) {
}


function controller($state, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const selector = mkSelectionOptions(vm.parentEntityRef);
        serviceBroker
            .loadViewData(
                CORE_API.ChangeInitiativeStore.findHierarchyBySelector,
                [ selector ])
            .then(r => {
                vm.rawInitiatives = r.data;
                vm.treeData = mkTreeData(vm.rawInitiatives, vm.parentEntityRef, vm.displayRetired);
            });
    };

    vm.$onChanges = (c) => {
        if (c.displayRetired) {
            vm.treeData = mkTreeData(vm.rawInitiatives, vm.parentEntityRef, vm.displayRetired);
        }
    };

    vm.onSelectNavItem = (item) => {
        if (item.id === vm.parentEntityRef.id || item.isFake) {
            return; // nothing to do, user clicked on self
        }
        $state.go(
            kindToViewState(entity.CHANGE_INITIATIVE.key),
            { id: item.id });
    };
}


controller.$inject = [
    "$state",
    "ServiceBroker"];


const component = {
    bindings,
    template,
    controller
};


export default {
    id: "waltzChangeInitiativeTree",
    component
};