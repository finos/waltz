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
import {mkLinkGridCell} from "../../../common/grid-utils";
import {mkApplicationSelectionOptions} from "../../../common/selector-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import {indexRatingSchemes} from "../../../ratings/rating-utils";

import template from "./measurable-ratings-browser-tree-panel.html";

/**
 * @name waltz-measurable-ratings-browser-tree-panel
 *
 * @description
 * This component shows a tabbed view (one tab per measurable category) of measurable rating trees.
 * Clicking on tree items shows a detail list on the right.
 *
 */


const bindings = {
    filters: "<",
    parentEntityRef: "<",
    onMeasurableCategorySelect: "<"
};


const initialState = {
    applications: [],
    measurables: [],
    measurableCategories: [],
    measurableRatingsDetail: null,
    ratingTallies: [],
    detail: null,
    visibility: {
        loading: false,
        ratingDetail: false,
    },
    selectedMeasurable: null,
    onLoadDetail: () => log("onLoadDetail")
};


function prepareColumnDefs(measurableCategory) {
    return [
        mkLinkGridCell("Name", "application.name", "application.id", "main.app.view"),
        {
            field: "application.assetCode",
            name: "Asset Code",
            width: "10%"
        },
        {
            field: "rating.name",
            name: "Rating",
            cellTemplate: "<div class=\"ui-grid-cell-contents\"><waltz-rating-indicator-cell rating=\"row.entity.rating\" show-name=\"true\"></waltz-rating-indicator-cell></div>",
            width: "10%"
        },
        {
            field: "measurable.name",
            name: measurableCategory.name
        },
        {
            field: "rating.description",
            name: "Comment"
        }
    ];
}

function prepareUnmappedColumnDefs() {
    return [
        mkLinkGridCell("Name", "application.name", "application.id", "main.app.view"),
        {
            field: "application.assetCode",
            name: "Asset Code",
            width: "50%"
        }
    ];
}


function findChildIds(measurable) {
    const recurse = (acc, n) => {
        acc.push(n.id);
        _.each(n.children, c => recurse(acc, c));
        return acc;
    };

    return recurse([], measurable);
}


function prepareTableData(measurable,
                          ratingScheme = {},
                          applications = [],
                          ratings = [],
                          measurablesById = {}) {

    const appsById = _.keyBy(applications, "id");
    const relevantMeasurableIds = findChildIds(measurable);

    const data = _
        .chain(ratings)
        .filter(r => _.includes(relevantMeasurableIds, r.measurableId))
        .map(r => {
            return {
                application: appsById[r.entityReference.id],
                rating: Object.assign(
                    {},
                    ratingScheme.ratingsByCode[r.rating],
                    { description: r.description}),
                measurable: measurablesById[r.measurableId]
            };
        })
        .value();

    return _.sortBy(data, "application.name");
}


function prepareUnmappedTableData(applications = [],
                                ratings = [],
                                measurables = [],
                                categoryId) {

    const measurableIdsOfACategory =
        _.chain(measurables)
            .filter(m => m.categoryId === categoryId)
            .map(m => m.id)
            .value();

    const appIdsWithMeasurable =
        _.chain(ratings)
            .filter(r => measurableIdsOfACategory.includes(r.measurableId))
            .map(r => r.entityReference.id)
            .value();

    const tableData =
        _.chain(applications)
            .filter(a => !appIdsWithMeasurable.includes(a.id))
            .map(app => {
                return {
                    application: app
                };
            }).sortBy("application.name")
            .value();

    return tableData;
}

function log() {
    console.log("wmrbs::", arguments);
}


function loadMeasurableRatings(serviceBroker, selector, holder) {
    return serviceBroker
        .loadViewData(CORE_API.MeasurableRatingStore.statsByAppSelector, [selector])
        .then(r => holder.ratingTallies = r.data);
}


function loadMeasurables(serviceBroker, selector, holder) {
    return serviceBroker
        .loadViewData(CORE_API.MeasurableStore.findMeasurablesBySelector, [selector])
        .then(r => {
            holder.measurables = r.data;
            holder.measurablesById = _.keyBy(r.data, "id");
        });
}


function loadMeasurableCategories(serviceBroker, holder) {
    return serviceBroker
        .loadAppData(CORE_API.MeasurableCategoryStore.findAll, [])
        .then(r => holder.measurableCategories = r.data);
}


function loadApps(serviceBroker, selector, holder) {
    return serviceBroker
        .loadViewData(CORE_API.ApplicationStore.findBySelector, [selector])
        .then(r => holder.applications = r.data);
}


function loadRatingSchemes(serviceBroker, holder) {
    return serviceBroker
        .loadAppData(CORE_API.RatingSchemeStore.findAll)
        .then(r => holder.ratingSchemesById = indexRatingSchemes(r.data));
}


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const clearDetail = () => {
        vm.measurableRatingsDetail = null;
        vm.tableData = null;
    };

    const loadBaseData = () => {
        vm.selector = mkApplicationSelectionOptions(vm.parentEntityRef,
            undefined,
            undefined,
            vm.filters);

        const promise = $q.all([
            loadMeasurableCategories(serviceBroker, vm),
            loadMeasurables(serviceBroker, vm.selector, vm),
            loadRatingSchemes(serviceBroker, vm),
            loadMeasurableRatings(serviceBroker, vm.selector, vm),
            loadApps(serviceBroker, vm.selector, vm)
        ]);

        return promise;
    };


    const loadRatings = () => {
        clearDetail();

        vm.selector = mkApplicationSelectionOptions(vm.parentEntityRef,
            undefined,
            undefined,
            vm.filters);

        const promise = $q.all([
            loadApps(serviceBroker, vm.selector, vm),
            loadMeasurableRatings(serviceBroker, vm.selector, vm),
        ]);

        if(vm.visibility.ratingDetail) {
            promise.then(() => vm.onSelect(vm.selectedMeasurable));
        }

        return promise;
    };


    const loadRatingDetail = () => {
        clearDetail();
        return vm.measurableRatingsDetail
            ? $q.resolve(vm.measurableRatingsDetail)
            : serviceBroker
                .execute(CORE_API.MeasurableRatingStore.findByAppSelector, [vm.selector])
                .then(r => vm.measurableRatingsDetail = r.data);
    };


    vm.$onInit = () => {
        loadBaseData()
            .then(() => loadRatings());
    };


    vm.$onChanges = (changes) => {
        if(vm.parentEntityRef && changes.filters) {
            loadRatings();
        }
    };

    vm.onSelectUnmapped = (categoryId) => {
        vm.selectedMeasurable = { name: "Unmapped Applications",
            description: "Display applications which do not have any associated measurable rating for this category."};
        loadUnmappedApplications(vm.measurables, categoryId);
    };

    vm.onSelect = (measurable) => {
        vm.visibility.ratingDetail = false;
        vm.visibility.loading = true;

        vm.tableData = null;
        vm.selectedMeasurable = measurable;
        const promise = loadRatingDetail();
        const category = _.find(vm.measurableCategories, ({ id: measurable.categoryId }));
        const ratingScheme = vm.ratingSchemesById[category.ratingSchemeId];

        vm.columnDefs = prepareColumnDefs(category);
        if (_.isFunction(_.get(promise, "then"))) {
            promise
                .then(ratings => vm.tableData = prepareTableData(
                    measurable,
                    ratingScheme,
                    vm.applications,
                    ratings,
                    vm.measurablesById))
                .then(() => {
                    vm.visibility.loading = false;
                    vm.visibility.ratingDetail = true;
                });
        } else {
            log("was expecting promise, got: ", promise);
            vm.visibility.loading = false;
        }
    };

    const loadUnmappedApplications = (measurables, measurableCategoryId) => {
        vm.columnDefs = prepareUnmappedColumnDefs();

        loadRatingDetail()
            .then(ratings => vm.tableData = prepareUnmappedTableData(
                vm.applications,
                ratings,
                measurables,
                measurableCategoryId))
            .then(() => {
                vm.visibility.loading = false;
                vm.visibility.ratingDetail = true;
            });
    };


    vm.onCategorySelect = (c) => {
        vm.visibility.ratingDetail = false;
        clearDetail();
        vm.activeCategory = c;
        vm.onMeasurableCategorySelect(c);
    };
}



controller.$inject = [
    "$q",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzMeasurableRatingsBrowserTreePanel"
};