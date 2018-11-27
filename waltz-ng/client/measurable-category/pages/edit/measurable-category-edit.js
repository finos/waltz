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
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import template from "./measurable-category-edit.html";
import {toEntityRef} from "../../../common/entity-utils";


const modes = {
    SUMMARY : "SUMMARY",
    NODE_VIEW: "NODE_VIEW",
    CHANGE_VIEW: "CHANGE_VIEW"
};


const initialState = {
    changeDomain: null,
    measurables: [],
    selectedMeasurable: null,
    selectedChange: null,
    recentlySelected: [],
    pendingChanges: [],
    mode: modes.SUMMARY
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
                    notification,
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
    };

    vm.onDiscardPendingChange = (change) => {
        const proceed = confirm("Are you sure that you wish to discard this change?");
        if (!proceed) { return Promise.resolve(false); }
        return serviceBroker
            .execute(
                CORE_API.TaxonomyManagementStore.removeById,
                [ change.id ])
            .then(() => {
                notification.info("Change discarded");
                reloadPending();
                return true;
            })
            .catch(e => {
                const msg = `Failed to discard change: ${e.message}`;
                notification.error(msg);
                console.error(msg, { e })
            });
    };

    vm.onApplyPendingChange = (change) => {
        return serviceBroker
            .execute(
                CORE_API.TaxonomyManagementStore.applyPendingChange,
                [ change.id ])
            .then(() => {
                notification.info("Change applied");
                reloadPending();
                return true;
            })
            .catch(e => {
                const message = `Error when applying command: ${_.get(e, ["data", "message"], "Unknown")}`;
                console.log(message, e);
                notification.error(message)
            });
    };

    vm.onSubmitChange = (change) => {
        return serviceBroker
            .execute(
                CORE_API.TaxonomyManagementStore.submitPendingChange,
                [ change ])
            .then(() => {
                notification.info("Change submitted");
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
    "Notification",
    "ServiceBroker"
];


export default {
    template,
    controller,
    controllerAs: "$ctrl"
};
