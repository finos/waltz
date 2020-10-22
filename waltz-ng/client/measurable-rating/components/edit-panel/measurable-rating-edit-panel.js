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
import {initialiseData} from "../../../common";
import {kindToViewState} from "../../../common/link-utils";
import {getDateAsUtc, loadAllData, loadDecommData, mkTabs} from "../../measurable-rating-utils";
import {indexRatingSchemes, mkRatingsKeyHandler} from "../../../ratings/rating-utils";

import template from "./measurable-rating-edit-panel.html";
import {displayError} from "../../../common/error-utils";


const bindings = {
    allocations: "<",
    allocationSchemes: "<",
    parentEntityRef: "<",
    startingCategoryId: "<?",
    plannedDecommissions: "<?",
    replacementApps: "<?",
    replacingDecommissions: "<?",
    application: "<"
};


const initialState = {
    activeTab: null,
    allocations: [],
    allocationSchemes: [],
    categories: [],
    measurables: [],
    plannedDecommissions: [],
    ratings: [],
    ratingSchemesById: {},
    ratingItemsBySchemeIdByCode: {},
    replacementApps: [],
    replacingDecommissions: [],
    saveInProgress: false,
    selected: null,
    startingCategoryId: null,
    tabs: [],
    unusedCategories: [],
    visibility: {
        ratingEditor: false,
        showDescriptionInPicker: false,
        instructions: true,
        schemeOverview: false,
        showAllCategories: false,
        tab: null
    }
};


function controller($q,
                    $state,
                    notification,
                    serviceBroker,
                    userService) {
    const vm = initialiseData(this, initialState);


    const loadData = (force) => {
        return loadAllData($q, serviceBroker, vm.parentEntityRef, true, force)
            .then((r) => {
                Object.assign(vm, r);
                recalcTabs();
                vm.categoriesById = _.keyBy(vm.categories, "id");
                vm.ratingItemsBySchemeIdByCode = indexRatingSchemes(_.values(vm.ratingSchemesById) || []);
                if (vm.startingCategoryId) {
                    vm.activeTab = _.find(vm.tabs, t => t.category.id === vm.startingCategoryId)
                } else if (!vm.activeTab) {
                    vm.activeTab = vm.tabs[0];
                }
                vm.onTabChange();
            })
    };

    const recalcTabs = function () {
        const hasNoRatings = vm.ratings.length === 0;
        const showAllCategories = hasNoRatings || vm.visibility.showAllCategories;
        const allTabs = mkTabs(vm, showAllCategories);
        vm.tabs = _.filter(allTabs, t => _.includes(vm.userRoles, t.category.ratingEditorRole));
        vm.hasHiddenTabs = vm.categories.length !== allTabs.length;
        if (vm.activeTab) {
            const ratingSchemeItems =  vm.activeTab.ratingSchemeItems;
            vm.activeTab = _.find(vm.tabs, t => t.category.id === vm.activeTab.category.id);
            vm.activeTab.ratingSchemeItems = ratingSchemeItems;
        }
    };

    const getDescription = () => _.get(vm.selected, ["rating", "description"], "");

    const getRating = () => _.get(vm.selected, ["rating", "rating"]);

    const doRatingSave = (rating, description) => {
        const currentRating = !_.isEmpty(vm.selected.rating) ? vm.selected.rating.rating : null;
        return serviceBroker
            .execute(
                CORE_API.MeasurableRatingStore.save,
                [vm.parentEntityRef, vm.selected.measurable.id, rating, currentRating, description])
            .then(r => { vm.ratings = r.data })
            .then(() => recalcTabs())
            .then(() => {
                vm.saveInProgress = false;
                const newRating = { rating, description };
                vm.selected = Object.assign({}, vm.selected, { rating: newRating });
            })
            .catch(e => {
                displayError(notification, "Could not save rating", e);
                throw e;
            })
    };

    const doRemove = () => {
        if (! vm.selected.rating) return $q.reject();

        vm.saveInProgress = true;

        return serviceBroker
            .execute(
                CORE_API.MeasurableRatingStore.remove,
                [vm.parentEntityRef, vm.selected.measurable.id])
            .then(r => {
                vm.saveInProgress = false;
                vm.ratings = r.data;
                vm.selected.rating = null;
                recalcTabs();
            });
    };

    const deselectMeasurable = () => {
        vm.saveInProgress = false;
        vm.selected = Object.assign({}, vm.selected, { measurable: null });
        vm.visibility = Object.assign({}, vm.visibility, {schemeOverview: true, ratingEditor: false});
    };

    const selectMeasurable = (node) => {
        const { measurable, allocations } = node;
        const category = vm.categoriesById[measurable.categoryId];
        const ratingScheme = vm.ratingSchemesById[category.ratingSchemeId];
        const hasWarnings = !_.isEmpty(allocations);

        vm.selected = Object.assign({}, node, { category, hasWarnings, ratingScheme });
        vm.visibility = Object.assign({}, vm.visibility, {schemeOverview: false, ratingEditor: true});
    };

    const reloadDecommData = () => {
        return loadDecommData($q, serviceBroker, vm.parentEntityRef, true)
            .then(r => Object.assign(vm, r));
    };

    const saveDecommissionDate = (dateChange)  => {

        if(_.isNil(dateChange.newVal)){
            notification.error("Could not save this decommission date. " +
                "Check the date entered is valid or to remove this decommission date use the 'Revoke' button below");
        } else {
            dateChange.newVal = getDateAsUtc(dateChange.newVal);

            serviceBroker
                .execute(
                    CORE_API.MeasurableRatingPlannedDecommissionStore.save,
                    [vm.parentEntityRef, vm.selected.measurable.id, dateChange])
                .then(r => {
                    const decom = Object.assign(r.data, {isValid: true});
                    vm.selected = Object.assign({}, vm.selected, {decommission: decom});
                    notification.success(`Saved decommission date for ${vm.selected.measurable.name}`);
                })
                .catch(e => displayError(notification, "Could not save decommission date", e))
                .finally(reloadDecommData);
        }
    };

    // -- BOOT --

    vm.$onInit = () => {

        userService
            .whoami()
            .then(user => vm.userRoles = user.roles)
            .then(() => loadData(true));

        vm.backUrl = $state
            .href(
                kindToViewState(vm.parentEntityRef.kind),
                { id: vm.parentEntityRef.id });
        vm.allocationsByMeasurableId = _.groupBy(vm.allocations, a => a.measurableId);
        vm.allocationSchemesById = _.keyBy(vm.allocationSchemes, s => s.id);
    };


    // -- INTERACT ---

    vm.onRemoveReplacementApp = (replacement) => {
        return serviceBroker
            .execute(
                CORE_API.MeasurableRatingReplacementStore.remove,
                [replacement.decommissionId, replacement.id])
            .then(r => {
                vm.selected = Object.assign({}, vm.selected, { replacementApps: r.data });
                notification.success("Replacement app removed")
            })
            .catch(e  => displayError(notification, "Could not remove replacement app", e))
            .finally(reloadDecommData);
    };

    vm.onAddReplacementApp = (replacement) => {
        return serviceBroker
            .execute(
                CORE_API.MeasurableRatingReplacementStore.save,
                [replacement.decommissionId, replacement])
            .then(r => {
                vm.selected = Object.assign({}, vm.selected, { replacementApps: r.data });
                notification.success("Successfully added replacement app")
            })
            .catch(e  => displayError(notification, "Could not add replacement app", e))
            .finally(reloadDecommData);
    };

    vm.checkPlannedDecomDateIsValid = (decomDate) => {

        const appDate = new Date(vm.application.plannedRetirementDate);
        const newDecomDate = new Date(decomDate);

        const sameDate = appDate.getFullYear() === newDecomDate.getFullYear()
            && appDate.getMonth() === newDecomDate.getMonth()
            && appDate.getDate() === newDecomDate.getDate();

        return appDate > newDecomDate || sameDate;
    };

    vm.onSaveDecommissionDate = (dateChange) => {

        if (vm.application.entityLifecycleStatus === "REMOVED"){
            notification.error("Decommission date cannot be set. This application is no longer active");
            return;
        }

        if (_.isNull(vm.application.plannedRetirementDate) || vm.checkPlannedDecomDateIsValid(dateChange.newVal)){
            saveDecommissionDate(dateChange);

        } else {
            const appDate = new Date(vm.application.plannedRetirementDate).toDateString();

            if (!confirm(`This decommission date is later then the planned retirement date of the application: ${appDate}. Are you sure you want to save it?`)){
                notification.error("Decommission date was not saved");
                return;
            }
            saveDecommissionDate(dateChange)
        }
    };

    vm.onRemoveDecommission = () => {
        if (!confirm("Are you sure you want to remove this decommission and any replacement apps?")) {
            notification.info("Revocation of decommission cancelled");
            return;
        }
        serviceBroker
            .execute(
                CORE_API.MeasurableRatingPlannedDecommissionStore.remove,
                [vm.selected.decommission.id])
            .then(() => {
                vm.selected = Object.assign({}, vm.selected, { decommission: null, replacementApps: [] });
                notification.success(`Removed decommission date and replacement applications for: ${vm.selected.measurable.name}`);
            })
            .catch(e => displayError(notification, "Could not remove decommission date", e))
            .finally(reloadDecommData);
    };

    vm.onMeasurableSelect = (node) => {
        selectMeasurable(node);
    };

    vm.onRatingSelect = r => {
        if (! vm.selected.measurable) return; // nothing selected
        if (! vm.selected.measurable.concrete) return; // not concrete
        if (r === getRating()) return; // rating not changed

        return r === "X"
            ? doRemove()
                .then(() => notification.success(`Removed: ${vm.selected.measurable.name}`))
            : doRatingSave(r, getDescription())
                .then(() => notification.success(`Saved: ${vm.selected.measurable.name}`))
    };

    vm.onSaveComment = (comment) => {
        return doRatingSave(getRating(), comment)
            .then(() => notification.success(`Saved Comment for: ${vm.selected.measurable.name}`))
    };

    vm.doCancel = () => {
        deselectMeasurable();
    };

    vm.onRemoveAll = (categoryId) => {
        if (confirm("Do you really want to remove all ratings in this category ?")) {
            serviceBroker
                .execute(
                    CORE_API.MeasurableRatingStore.removeByCategory,
                    [vm.parentEntityRef, categoryId])
                .then(r => {
                    notification.info("Removed all ratings for category");
                    vm.ratings = r.data;
                    recalcTabs();
                })
                .catch(e => {
                    const message = "Error removing all ratings for category: " + e.message;
                    notification.error(message);
                });
        }
    };


    vm.onTabChange = () => {
        deselectMeasurable();

        if(_.isUndefined(vm.activeTab)){
            vm.activeTab = _.first(vm.tabs);
        }

        serviceBroker
            .loadViewData(
                CORE_API.RatingSchemeStore.findRatingsForEntityAndMeasurableCategory,
                [vm.parentEntityRef, vm.activeTab.category.id])
            .then(r => {
                vm.activeTab.ratingSchemeItems = r.data;
            });

        vm.onKeypress = mkRatingsKeyHandler(
            vm.activeTab.ratingSchemeItems,
            vm.onRatingSelect,
            vm.doCancel);
    };

    vm.onShowAllTabs = () => {
        vm.visibility.showAllCategories = true;
        recalcTabs();
    };

}


controller.$inject = [
    "$q",
    "$state",
    "Notification",
    "ServiceBroker",
    "UserService"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzMeasurableRatingEditPanel"
};
