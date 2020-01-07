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
import {mkLinkGridCell} from "../../../common/grid-utils";
import {mkRatingSchemeColorScale} from "../../../common/colors";
import {mkSelectionOptions} from "../../../common/selector-utils";

import template from "./measurable-rating-explorer-section.html";

/**
 * @name waltz-measurable-rating-explorer-section
 *
 * @description
 * This component provides an overview pie chart showing a breakdown of measurable
 * ratings by their rated values.  It also provides a detail table showing
 * all the ratings and their associated applications.
 */


const bindings = {
    filters: "<",
    parentEntityRef: "<"
};


const initialState = {
    measurables: [],
    query: "",
    pie: null,
    ratings: [],
    visibility: {
        ratingOverlay: false
    }
};


function preparePie(ratings = [],
                    ratingScheme = {},
                    onSelect) {
    const counts = _.countBy(ratings, "rating");
    const schemeItemsByCode = _.keyBy(ratingScheme.ratings, "rating" );
    const colorScale = mkRatingSchemeColorScale(ratingScheme);

    const data = _.chain(ratingScheme.ratings)
        .reject(schemeItem => schemeItem.rating === "X")
        .map(schemeItem => ({
            key: schemeItem.rating,
            count: counts[schemeItem.rating] || 0
        }))
        .value();

    return {
        selectedSegmentKey: null,
        data,
        config: {
            size: 130,
            onSelect,
            colorProvider: (d) => colorScale(d.key),
            labelProvider: (d) => _.get(schemeItemsByCode, [d.key, "name"], d.key),
            descriptionProvider: (d) => _.get(schemeItemsByCode, [d.key, "description"], d.key)
        }
    };
}


function prepareTableData(ratings = [],
                          measurables = [],
                          scheme = {},
                          appsById = {}) {
    const measurablesById = _.keyBy(measurables, "id");
    const ratingItemsByCode = _.keyBy(scheme.ratings, r => r.rating);

    return _.chain(ratings)
        .map(r => {
            return {
                rating: ratingItemsByCode[r.rating],
                measurable: measurablesById[r.measurableId],
                entityReference: Object.assign({}, r.entityReference, { assetCode: appsById[r.entityReference.id].assetCode }),
                description: r.description
            };
        })
        .value();
}


const ratingCellTemplate = `
    <div class="ui-grid-cell-contents">
        <waltz-rating-indicator-cell rating="row.entity.rating"
                                     show-description-popup="true"
                                     show-name="true">
        </waltz-rating-indicator-cell>
    </div>`;


function prepareColumnDefs(measurableCategory, measurables) {
    const initialCols = [
        mkLinkGridCell("Name", "entityReference.name", "entityReference.id", "main.app.view"),
        {
            field: "entityReference.assetCode",
            name: "Asset Code"
        }, {
            field: "rating",
            name: "Rating",
            cellTemplate: ratingCellTemplate,
            sortingAlgorithm: (a, b) => a.name.localeCompare(b.name),
            exportFormatter: (input) => input.name
        }
    ];

    // We only want to show the measurable column if there are multiple measurables to
    // differentiate between.
    const measurableCols = measurables.length > 1
        ? [ { field: "measurable.name", name: measurableCategory.name } ]
        : [];

    const finalCols = [{
        field: "description",
        name: "Comment",
        cellTemplate: `<span class="waltz-entity-icon-label"
                      uib-popover-template="'mres/desc-popup.html'"
                      popover-trigger="mouseenter"
                      popover-enable="true"
                      popover-class="waltz-popover-wide"
                      popover-placement="top-right"
                      popover-append-to-body="true">
                      {{row.entity.description | limitTo: 25 }}
                      {{row.entity.description.length > 25 ? '...' : ''}}
                </span>`
    }];

    return [].concat(initialCols, measurableCols, finalCols);
}


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const onSelect = (d) => {
        if (_.get(d, "count", 0) === 0) return;

        vm.pie.selectedSegmentKey = d ? d.key : null;
        const ratings = d
                ? _.filter(vm.ratings, r => r.rating === d.key)
                : vm.ratings;

        vm.tableData = prepareTableData(
            ratings,
            vm.measurables,
            vm.ratingScheme,
            vm.appsById);
    };

    const loadData = () => {
        const selector = mkSelectionOptions(
            vm.parentEntityRef,
            undefined,
            undefined,
            vm.filters);

        const appsPromise = serviceBroker
            .loadViewData(CORE_API.ApplicationStore.findBySelector, [ selector ])
            .then(r => vm.appsById = _.keyBy(r.data, "id"));

        const schemePromise = serviceBroker
            .loadAppData(CORE_API.RatingSchemeStore.findAll)
            .then(r => vm.ratingSchemes = r.data);

        const ratingsPromise = serviceBroker
            .loadViewData(
                CORE_API.MeasurableRatingStore.findByMeasurableSelector,
                [ selector ])
            .then(r => vm.ratings = r.data);

        const measurablesPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll)
            .then(result => vm.measurables = result.data);

        const categoriesPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
            .then(result => vm.measurableCategories = result.data);

        return $q.all([appsPromise, ratingsPromise, measurablesPromise, categoriesPromise, schemePromise]);
    };

    const processData = () => {
        const measurable = _.find(vm.measurables, { id: vm.parentEntityRef.id });
        const measurableCategory = _.find(vm.measurableCategories, { id: measurable.categoryId });
        vm.ratingScheme = _.find(vm.ratingSchemes, { id: measurableCategory.ratingSchemeId });

        vm.columnDefs = prepareColumnDefs(
            measurableCategory,
            vm.measurables);
        vm.pie = preparePie(
            vm.ratings,
            vm.ratingScheme,
            onSelect);
        vm.tableData = prepareTableData(
            vm.ratings,
            vm.measurables,
            vm.ratingScheme,
            vm.appsById);
    };

    vm.$onInit = () => loadData()
        .then(() => processData());

    vm.$onChanges = () => loadData()
        .then(() => processData());

    vm.onGridInitialise = (cfg) =>
        vm.exportData = () => cfg.exportFn("measurable-ratings.csv");
}


controller.$inject = [
    "$q",
    "ServiceBroker"];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzMeasurableRatingExplorerSection"
};
