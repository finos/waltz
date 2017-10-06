/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import _ from 'lodash';
import {initialiseData} from '../../../common';
import {CORE_API} from '../../../common/services/core-api-utils';
import {mkLinkGridCell} from '../../../common/link-utils';
import {ragColorScale} from '../../../common/colors';

/**
 * @name waltz-measurable-rating-explorer-section
 *
 * @description
 * This component provides an overview pie chart showing a breakdown of measurable
 * ratings by their rated values.  It also provides a detail table showing
 * all the ratings and their associated applications.
 */

import template from './measurable-rating-explorer-section.html';
import {mkSelectionOptions} from "../../../common/selector-utils";


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
                    ragNames = {},
                    onSelect) {
    const counts = _.countBy(ratings, 'rating');
    const data = [
        { key: "R", count: counts['R'] || 0 },
        { key: "A", count: counts['A'] || 0 },
        { key: "G", count: counts['G'] || 0 },
        { key: "Z", count: counts['Z'] || 0 },
    ];

    return {
        selectedSegmentKey: null,
        data,
        config: {
            size: 130,
            onSelect,
            colorProvider: (d) => ragColorScale(d.data.key),
            labelProvider: (d) => ragNames[d.key] ? ragNames[d.key].name : d.key,
            descriptionProvider: (d) => ragNames[d.key] ? ragNames[d.key].description : d.key
        }
    };
}


function prepareTableData(ratings = [],
                          applications = [],
                          measurables = [],
                          ragNames = {}) {
    const measurablesById = _.keyBy(measurables, 'id');
    const applicationsById = _.keyBy(applications, 'id');
    return _.chain(ratings)
        .map(r => {
            return {
                rating: r,
                ratingName: ragNames[r.rating] || r.rating,
                measurable: measurablesById[r.measurableId],
                application: applicationsById[r.entityReference.id]
            };
        })
        .value();
}


const ratingCellTemplate = `
    <div class="ui-grid-cell-contents">
        <waltz-rating-indicator-cell rating="row.entity.rating.rating" 
                                     label="COL_FIELD.name">
        </waltz-rating-indicator-cell>
    </div>`;


function prepareColumnDefs(measurableCategory, measurables) {
     // We only want to show the measurable column if there are multiple measurables to
     // differentiate between.

    const initialCols = [
        mkLinkGridCell('Name', 'application.name', 'application.id', 'main.app.view'),
        {
            field: 'application.assetCode',
            name: 'Asset Code'
        },
        {
            field: 'ratingName',
            name: 'Rating',
            cellTemplate: ratingCellTemplate
        }
    ];

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
        vm.pie.selectedSegmentKey = d ? d.key : null;

        const ratings = d
                ? _.filter(vm.ratings, r => r.rating === d.key)
                : vm.ratings;

        vm.tableData = prepareTableData(
            ratings,
            vm.applications,
            vm.measurables,
            vm.measurableCategory.ragNames);
    };

    const loadData = () => {
        const selector = mkSelectionOptions(vm.parentEntityRef);

        const appPromise = serviceBroker
            .loadViewData(
                CORE_API.ApplicationStore.findBySelector,
                [ selector ])
            .then(r => vm.applications = r.data);

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


        return $q.all([appPromise, ratingsPromise, measurablesPromise, categoriesPromise]);
    };

    const processData = () => {
        const measurable = _.find(vm.measurables, { id: vm.parentEntityRef.id });
        const measurableCategory = _.find(vm.measurableCategories, { id: measurable.categoryId });
        vm.columnDefs = prepareColumnDefs(
            measurableCategory,
            vm.measurables);
        vm.pie = preparePie(
            vm.ratings,
            measurableCategory.ragNames,
            onSelect);
        vm.tableData = prepareTableData(
            vm.ratings,
            vm.applications,
            vm.measurables,
            measurableCategory.ragNames);
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