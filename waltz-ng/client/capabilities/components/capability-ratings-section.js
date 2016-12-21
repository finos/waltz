/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import {initialiseData, mkLinkGridCell} from "../../common";
import {ragColorScale} from "../../common/colors";
import {applicationRatingNames} from "../../common/services/display-names";

const bindings = {
    appCapabilities: '<',
    apps: '<',
    sourceDataRatings: '<'
};


const initialState = {
    appCapabilities: [],
    apps: [],
    visibility: {
        ratingOverlay: false
    },
    query: '',
    pie: {
        selectedSegmentKey: null,
        data: [],
        config: null
    },
    columnDefs: [
        mkLinkGridCell('Name', 'app.name', 'app.id', 'main.app.view'),
        {
            field: 'app.assetCode',
            name: 'Asset Code'
        }, {
            field: 'ratingName',
            name: 'Rating',
            cellTemplate: '<div class="ui-grid-cell-contents"><waltz-rating-indicator-cell rating="row.entity.rating" label="COL_FIELD"></waltz-rating-indicator-cell></div>'
        }, {
            field: 'description',
            name: 'Comment'
        },
    ]
};


const template = require('./capability-ratings-section.html');


function prepareAppCapabilities(appCapabilities = [], apps = []) {
    const appsById = _.keyBy(apps, "id");

    return _.map(
        appCapabilities,
        ac => Object.assign({},
            ac,
            { app: appsById[ac.applicationId] },
            { ratingName: applicationRatingNames[ac.rating] || ac.rating }
        ));
}


function preparePie(appCapabilities = [], displayNameService, onSelect = () => null) {
    const config = {
        colorProvider: (d) => ragColorScale(d.data.key),
        labelProvider: (d) => displayNameService.lookup('applicationRating', d.key),
        onSelect
    };
    const counts = _.countBy(appCapabilities, "rating");
    const data =  [
        { key: "R", count: counts['R'] || 0 },
        { key: "A", count: counts['A'] || 0 },
        { key: "G", count: counts['G'] || 0 },
        { key: "Z", count: counts['Z'] || 0 },
    ];

    return {
        config,
        data
    }
}


function controller(displayNameService) {
    let rawTableData = [];

    const vm = initialiseData(this, initialState);

    const onSelect = (d) => {
        vm.pie.selectedSegmentKey = d ? d.key : null;

        vm.tableData = d
            ? _.filter(rawTableData, r => r.rating === d.key)
            : rawTableData;
    };

    vm.$onChanges = () => {
        rawTableData = prepareAppCapabilities(vm.appCapabilities, vm.apps);
        vm.tableData = rawTableData;

        vm.pie = preparePie(vm.appCapabilities, displayNameService, onSelect);
    };

    vm.onGridInitialise = (cfg) => vm.exportData = () => cfg.exportFn("apps-capability.csv");
}


controller.$inject = ['DisplayNameService'];


const component = {
    template,
    bindings,
    controller
};

export default component;