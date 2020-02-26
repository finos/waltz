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