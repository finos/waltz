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
import {initialiseData, buildHierarchies, switchToParentIds, mkLinkGridCell} from '../../../common';
import {measurableKindNames, capabilityRatingNames} from '../../../common/services/display-names';

/**
 * @name waltz-measurable-ratings-browser
 *
 * @description
 * This component ...
 */


const bindings = {
    applications: '<',
    measurables: '<',
    ratings: '<',
    onLoadDetail: '<'
};


const initialState = {
    applications: [],
    measurables: [],
    ratings: [],
    detail: null,
    visibility: {
        loading: false
    },
    selectedMeasurable: null,
    onLoadDetail: () => log('onLoadDetail')
};


const template = require('./measurable-ratings-browser-section.html');


function prepareColumnDefs(measurableKind) {
    return [
        mkLinkGridCell('Name', 'application.name', 'application.id', 'main.app.view'),
        {
            field: 'application.assetCode',
            name: 'Asset Code'
        },
        {
            field: 'ratingName',
            name: 'Rating',
            cellTemplate: '<div class="ui-grid-cell-contents"><waltz-rating-indicator-cell rating="row.entity.rating.rating" label="COL_FIELD"></waltz-rating-indicator-cell></div>'
        },
        {
            field: 'measurable.name',
            name: measurableKindNames[measurableKind]
        },
        {
            field: 'rating.description',
            name: 'Comment'
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
                          applications = [],
                          ratings = [],
                          measurablesById = {}) {

    const appsById = _.keyBy(applications, 'id');
    const relevantMeasurableIds = findChildIds(measurable);

    const data = _
        .chain(ratings)
        .filter(r => _.includes(relevantMeasurableIds, r.measurableId))
        .map(r => {
            return {
                application: appsById[r.entityReference.id],
                rating: r,
                measurable: measurablesById[r.measurableId],
                ratingName: capabilityRatingNames[r.rating]
            };
        })
        .value();

    return _.sortBy(data, 'application.name');
}


function log() {
    console.log("wmrbs::", arguments);
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (c) => {
        if (c.measurables) {
            vm.measurablesById = _.keyBy(vm.measurables, 'id');
        }
    };

    vm.onSelect = (measurable) => {
        vm.visibility.loading = true;
        vm.tableData = null;
        vm.selectedMeasurable = measurable;
        const promise = vm.onLoadDetail(measurable);

        vm.columnDefs = prepareColumnDefs(measurable.kind);
        if (_.isFunction(_.get(promise, 'then'))) {
            promise
                .then(ratings => vm.tableData = prepareTableData(
                    measurable,
                    vm.applications,
                    ratings,
                    vm.measurablesById))
                .then(() => vm.visibility.loading = false);
        } else {
            log('was expecting promise, got: ', promise);
            vm.visibility.loading = false;
        }
    };
}


controller.$inject = [
    '$q'
];


const component = {
    template,
    bindings,
    controller
};


export default component;