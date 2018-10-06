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
import {CORE_API} from "../../../common/services/core-api-utils";
import {timeFormat} from "d3-time-format";
import {initialiseData} from "../../../common";
import {kindToViewState} from "../../../common/link-utils";
import {mkTabs} from "../../measurable-rating-utils";
import {indexRatingSchemes, mkRatingsKeyHandler} from "../../../ratings/rating-utils";

import template from "./measurable-rating-edit-panel.html";


const bindings = {
    parentEntityRef: "<",
    startingCategoryId: "<"
};


function determineSaveFn(selected, store) {
    return _.isEmpty(selected.rating)
        ? store.create
        : store.update;
}


const initialState = {
    selected: null,
    measurables: [],
    categories: [],
    plannedDateCache: {},
    ratings: [],
    ratingSchemesById: {},
    ratingItemsBySchemeIdByCode: {},
    tabs: [],
    saveInProgress: false,
    startingCategoryId: 0,
    unusedCategories: [],
    visibility: {
        showAllCategories: false,
        tab: null
    }
};


function controller($q,
                    $state,
                    measurableRatingStore,
                    notification,
                    serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        loadData(true);
    };


    const recalcTabs = function () {
        vm.tabs = mkTabs(
            vm.categories,
            vm.ratingSchemesById,
            vm.measurables,
            vm.ratings,
            vm.visibility.showAllCategories);

        vm.hasHiddenTabs = vm.categories.length !== vm.tabs.length;
    };

    const loadData = (force) => {
        // -- LOAD ---

        const ratingSchemePromise = serviceBroker
            .loadAppData(CORE_API.RatingSchemeStore.findAll)
            .then(r => {
                vm.ratingSchemesById = _.keyBy(r.data, "id");
                vm.ratingItemsBySchemeIdByCode = indexRatingSchemes(r.data || []);
            });

        const categoryPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
            .then(r => {
                vm.categories = r.data;
                vm.categoriesById = _.keyBy(r.data, "id");
            });

        const measurablesPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll)
            .then(r => vm.measurables = r.data);

        const ratingsPromise = serviceBroker
            .loadViewData(CORE_API.MeasurableRatingStore.findForEntityReference, [ vm.parentEntityRef ], { force })
            .then(r => vm.ratings = r.data);

        $q.all([ratingsPromise, measurablesPromise, categoryPromise, ratingSchemePromise])
            .then(() => {
                recalcTabs();
                vm.onTabChange(vm.startingCategoryId);
            });

    };

    // -- INTERACT ---

    const getDescription = () => vm.selected.rating
        ? vm.selected.rating.description
        : null;

    const getRating = () => vm.selected.rating
        ? vm.selected.rating.rating
        : null;

    const getPlannedDate = () => vm.selected.rating
        ? vm.selected.rating.plannedDate
        : null;

    const doSave = (rating, description, plannedDate) => {
        const saveFn = determineSaveFn(vm.selected, measurableRatingStore);

        const savePromise = saveFn(
            vm.parentEntityRef,
            vm.selected.measurable.id,
            rating,
            description,
            plannedDate);

        return savePromise
            .then(rs => vm.ratings = rs)
            .then(() => recalcTabs())
            .then(() => {
                vm.saveInProgress = false;
                const newRating = { rating, description, plannedDate };
                vm.selected = Object.assign({}, vm.selected, { rating: newRating });
            });
    };

    const doRemove = () => {

        if (! vm.selected.rating) return $q.reject();

        vm.saveInProgress = true;

        return measurableRatingStore
            .remove(vm.parentEntityRef, vm.selected.measurable.id)
            .then(rs => {
                vm.saveInProgress = false;
                vm.ratings = rs;
                vm.tabs = mkTabs(vm.categories, vm.ratingSchemesById, vm.measurables, vm.ratings);
                vm.selected.rating = null;
            });
    };

    const reset = () => {
        vm.saveInProgress = false;
        vm.selected = {};
    };


    // If the new rating is one that doesn't require a plan date, it should be set to null
    // we persist the old one locally to allow easy retrieval
    const resetPlannedDate = (ratingSchemeId, measurableId, oldRating, newRating) => {
        if(getPlannedDate()) {
            //cache existing date
            _.set(vm.plannedDateCache, `${ratingSchemeId}.${measurableId}.${oldRating}`, getPlannedDate());
        }

        if (vm.getNeedsPlannedDate(ratingSchemeId, newRating)) {
            return _.get(vm.plannedDateCache, `${ratingSchemeId}.${measurableId}.${newRating}`, null);
        } else {
            return null;
        }
    };


    vm.backUrl = $state
        .href(
            kindToViewState(vm.parentEntityRef.kind),
            { id: vm.parentEntityRef.id });

    vm.onMeasurableSelect = (measurable, rating) => {
        const category = _.find(vm.categories, ({ id: measurable.categoryId }));
        vm.selected = Object.assign({}, vm.selected, { rating, measurable, category });
    };

    vm.onRatingSelect = r => {
        if (! vm.selected.measurable) return; // nothing selected
        if (! vm.selected.measurable.concrete) return; // not concrete
        if (r === getRating()) return; // rating not changed

        const plannedDate = resetPlannedDate(vm.selected.ratingScheme.id, vm.selected.measurable.id, getRating(), r);
        return r === "X"
            ? doRemove()
                .then(() => notification.success("Removed"))
            : doSave(r, getDescription(), plannedDate)
                .then(() => notification.success("Saved"));
    };

    vm.onSaveComment = (comment) => {
        return doSave(getRating(), comment, getPlannedDate())
            .then(() => notification.success("Saved Comment"))
    };

    vm.doCancel = reset;

    vm.onTabChange = (categoryId) => {
        vm.visibility.tab = categoryId;
        reset();
        const category = vm.categoriesById[categoryId];
        const ratingScheme = vm.ratingSchemesById[category.ratingSchemeId];
        vm.selected = {
            category,
            ratingScheme,
        };
        vm.onKeypress = mkRatingsKeyHandler(
            ratingScheme.ratings,
            vm.onRatingSelect,
            vm.doCancel);
    };

    vm.onShowAllTabs = () => {
        vm.visibility.showAllCategories = true;
        recalcTabs();
    };

    vm.getNeedsPlannedDate = (ratingSchemeId, ratingCode) => {
        if(ratingSchemeId && ratingCode) {
            return vm.ratingItemsBySchemeIdByCode[ratingSchemeId]
                .ratingsByCode[ratingCode]
                .needsPlannedDate;
        }
        return false;
    };

    vm.onUpdatePlannedDate = (itemId, data) => {
        const newPlannedDate = timeFormat("%Y-%m-%d")(data.newVal);
        return doSave(getRating(), getDescription(), newPlannedDate)
            .then(() => notification.success("Saved Planned Date"))
    };
}


controller.$inject = [
    "$q",
    "$state",
    "MeasurableRatingStore",
    "Notification",
    "ServiceBroker"
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
