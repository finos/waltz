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
import {initialiseData} from "../../../common";
import {kindToViewState} from "../../../common/link-utils";

import template from "./measurable-rating-edit-panel.html";
import {determineStartingTab, mkTabs} from "../../measurable-rating-utils";
import {mkRatingsKeyHandler} from "../../../ratings/rating-utils";


const bindings = {
    parentEntityRef: '<'
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
    ratings: [],
    tabs: [],
    saveInProgress: false,
    visibility: {
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


    const loadData = (force) => {
        // -- LOAD ---

        const ratingSchemePromise = serviceBroker
            .loadAppData(CORE_API.RatingSchemeStore.findAll)
            .then(r => vm.ratingSchemesById = _.keyBy(r.data, 'id'));

        const categoryPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
            .then(r => {
                vm.categories = r.data;
                vm.categoriesById = _.keyBy(r.data, 'id');
            });

        const measurablesPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll)
            .then(r => vm.measurables = r.data);

        const ratingsPromise = serviceBroker
            .loadViewData(CORE_API.MeasurableRatingStore.findByAppSelector, [{ entityReference: vm.parentEntityRef, scope: 'EXACT' }], { force })
            .then(r => vm.ratings = r.data);

        serviceBroker
            .loadViewData(CORE_API.ApplicationStore.getById, [vm.parentEntityRef.id], { force })
            .then(r => vm.entityRating = r.data.overallRating);

        $q.all([ratingsPromise, measurablesPromise, categoryPromise, ratingSchemePromise])
            .then(() => {
                vm.tabs = mkTabs(vm.categories, vm.ratingSchemesById, vm.measurables, vm.ratings);
                const startingTab = determineStartingTab(vm.tabs);
                vm.onTabChange(startingTab.category.id);
            });

        serviceBroker
            .loadViewData(CORE_API.PerspectiveDefinitionStore.findAll, [], { force })
            .then(r => vm.perspectiveDefinitions = r.data);
    };

    // -- INTERACT ---

    const getDescription = () => vm.selected.rating
        ? vm.selected.rating.description
        : null;

    const getRating = () => vm.selected.rating
        ? vm.selected.rating.rating
        : null;

    const doSave = (rating, description) => {
        const saveFn = determineSaveFn(vm.selected, measurableRatingStore);

        const savePromise = saveFn(
            vm.parentEntityRef,
            vm.selected.measurable.id,
            rating,
            description);

        return savePromise
            .then(rs => vm.ratings = rs)
            .then(() => vm.tabs = mkTabs(vm.categories, vm.ratingSchemesById, vm.measurables, vm.ratings))
            .then(() => {
                vm.saveInProgress = false;
                const newRating = { rating, description };
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

        return r === 'X'
            ? doRemove()
                .then(() => notification.success('Removed'))
            : doSave(r, getDescription())
                .then(() => notification.success('Saved'));
    };

    vm.onSaveComment = (comment) => {
        return doSave(getRating(), comment)
            .then(() => notification.success('Saved Comment'))
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


}


controller.$inject = [
    '$q',
    '$state',
    'MeasurableRatingStore',
    'Notification',
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzMeasurableRatingEditPanel'
};
