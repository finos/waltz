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
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import template from "./measurable-category-edit.html";
import {toEntityRef} from "../../../common/entity-utils";
import toasts from "../../../svelte-stores/toast-store";
import BulkTaxonomyEditor from "../../components/bulk-taxonomy-editor/BulkTaxonomyEditor.svelte";


const modes = {
    SUMMARY : "SUMMARY",
    NODE_VIEW: "NODE_VIEW",
    CHANGE_VIEW: "CHANGE_VIEW"
};

const tabs = {
    INTERACTIVE_TAXONOMY: "INTERACTIVE_TAXONOMY",
    BULK_TAXONOMY: "BULK_TAXONOMY",
    BULK_RATING: "BULK_RATING"
};

const initialState = {
    changeDomain: null,
    measurables: [],
    selectedMeasurable: null,
    selectedSiblings: [],
    selectedChange: null,
    recentlySelected: [],
    pendingChanges: [],
    mode: modes.SUMMARY,
    activeTab: tabs.BULK_TAXONOMY, // tabs.INTERACTIVE_TAXONOMY,
    BulkTaxonomyEditor
};


function loadChangesByDomain(serviceBroker, changeDomain) {
    if (!changeDomain) {
        return Promise.resolve([]);
    }
    return serviceBroker
        .loadViewData(
            CORE_API.TaxonomyManagementStore.findPendingChangesByDomain,
            [ changeDomain ],
            { force: true })
        .then(r => r.data);
}


function controller($q,
                    $state,
                    $stateParams,
                    serviceBroker) {

    const vm = initialiseData(this, initialState);
    const categoryId = $stateParams.id;

    // -- util

    function reloadPending() {
        loadChangesByDomain(serviceBroker, toEntityRef(vm.category))
            .then(cs => vm.pendingChanges = cs);
    }

    const clearSelections = () => {
        vm.selectedMeasurable = null;
        vm.selectedChange = null;
        vm.selectedSiblings = [];
    };

    const reloadMeasurables = () => {
        serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll, [], { force: true })
            .then(r => vm.measurables = _.filter(r.data, m => m.categoryId === categoryId));
    };

    // -- boot

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll)
            .then(r => vm.measurables = _.filter(r.data, m => m.categoryId === categoryId));

        serviceBroker
            .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
            .then(r => vm.category = _.find(r.data, { id: categoryId }))
            .then(reloadPending);
    };


    // -- interact

    vm.onSelect = (treeNode) => {
        clearSelections();
        vm.mode = modes.NODE_VIEW;
        vm.recentlySelected = _.unionBy(vm.recentlySelected, [treeNode], d => d.id);
        vm.selectedMeasurable = treeNode;
        vm.selectedSiblings = _
            .chain(vm.measurables)
            .filter(d => d.parentId === treeNode.parentId)
            .orderBy([d => d.position, d => d.name])
            .value();
    };

    vm.onDiscardPendingChange = (change) => {
        const proceed = confirm("Are you sure that you wish to discard this change?");
        if (!proceed) { return Promise.resolve(false); }
        return serviceBroker
            .execute(
                CORE_API.TaxonomyManagementStore.removeById,
                [ change.id ])
            .then(() => {
                toasts.info("Change discarded");
                reloadPending();
                return true;
            })
            .catch(e => {
                const msg = `Failed to discard change: ${e.message}`;
                toasts.error(msg);
                console.error(msg, { e })
            });
    };

    vm.onApplyPendingChange = (change) => {
        return serviceBroker
            .execute(
                CORE_API.TaxonomyManagementStore.applyPendingChange,
                [ change.id ])
            .then(() => {
                toasts.info("Change applied");
                reloadMeasurables();
                reloadPending();
                return true;
            })
            .catch(e => {
                const message = `Error when applying command: ${_.get(e, ["data", "message"], "Unknown")}`;
                console.log(message, e);
                toasts.error(message)
            });
    };

    vm.onSubmitChange = (change) => {
        return serviceBroker
            .execute(
                CORE_API.TaxonomyManagementStore.submitPendingChange,
                [ change ])
            .then(() => {
                toasts.info("Change submitted");
                reloadPending();
            });
    };

    vm.onDismissSelection = () => {
        clearSelections();
        vm.mode = modes.SUMMARY;
    };

    vm.onClearRecentlyViewed = () => {
        vm.recentlySelected = [];
    };

}


controller.$inject = [
    "$q",
    "$state",
    "$stateParams",
    "ServiceBroker"
];


export default {
    template,
    controller,
    controllerAs: "$ctrl"
};
