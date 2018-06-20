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
import {mkLinkGridCell} from "../../../common/grid-utils";
import {mkRatingSchemeColorScale} from "../../../common/colors";
import template from "./measurable-rating-explorer-section.html";
import {mkSelectionOptions} from "../../../common/selector-utils";

/**
 * @name waltz-measurable-rating-explorer-section
 *
 * @description
 * This component provides an overview pie chart showing a breakdown of measurable
 * ratings by their rated values.  It also provides a detail table showing
 * all the ratings and their associated applications.
 */


const bindings = {
    parentEntityRef: '<'
};


const initialState = {
    measurables: [],
    query: '',
    pie: null,
    ratings: [],
    visibility: {
        ratingOverlay: false
    }
};


function preparePie(ratings = [],
                    ratingScheme = {},
                    onSelect) {
    const counts = _.countBy(ratings, 'rating');
    const schemeItemsByCode = _.keyBy(ratingScheme.ratings, 'rating' );
    const colorScale = mkRatingSchemeColorScale(ratingScheme);

    const data = _.chain(ratingScheme.ratings)
        .reject(schemeItem => schemeItem.rating === 'X')
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
            colorProvider: (d) => colorScale(d.data.key),
            labelProvider: (d) => _.get(schemeItemsByCode, [d.key, "name"], d.key),
            descriptionProvider: (d) => _.get(schemeItemsByCode, [d.key, "description"], d.key)
        }
    };
}


function prepareTableData(ratings = [],
                          measurables = [],
                          scheme = {},
                          appsById = {}) {
    const measurablesById = _.keyBy(measurables, 'id');
    const ratingItemsByCode = _.keyBy(scheme.ratings, r => r.rating);

    return _.chain(ratings)
        .map(r => {
            return {
                rating: ratingItemsByCode[r.rating],
                measurable: measurablesById[r.measurableId],
                entityReference: Object.assign({}, r.entityReference, { assetCode: appsById[r.entityReference.id].assetCode })
            };
        })
        .value();
}


const ratingCellTemplate = `
    <div class="ui-grid-cell-contents">
        <waltz-rating-indicator-cell rating="row.entity.rating" 
                                     show-name="true">
        </waltz-rating-indicator-cell>
    </div>`;


function prepareColumnDefs(measurableCategory, measurables) {
    const initialCols = [
        mkLinkGridCell('Name', 'entityReference.name', 'entityReference.id', 'main.app.view'),
        {
            field: 'entityReference.assetCode',
            name: 'Asset Code'
        }, {
            field: 'rating',
            name: 'Rating',
            cellTemplate: ratingCellTemplate,
            sortingAlgorithm: (a, b) => a.name.localeCompare(b.name),
            exportFormatter: (input) => input.name
        }
    ];

    // We only want to show the measurable column if there are multiple measurables to
    // differentiate between.
    const measurableCols = measurables.length > 1
        ? [ { field: 'measurable.name', name: measurableCategory.name } ]
        : [];

    const finalCols = [{
        field: 'rating.description',
        name: 'Comment'
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
        const selector = mkSelectionOptions(vm.parentEntityRef);

        const appsPromise = serviceBroker
            .loadViewData(CORE_API.ApplicationStore.findBySelector, [ selector ])
            .then(r => vm.appsById = _.keyBy(r.data, 'id'));

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

    vm.$onChanges = (c) => loadData()
        .then(() => processData());

    vm.onGridInitialise = (cfg) =>
        vm.exportData = () => cfg.exportFn("measurable-ratings.csv");
}


controller.$inject = [
    '$q',
    'ServiceBroker'];


const component = {
    template,
    bindings,
    controller
};


export default component;