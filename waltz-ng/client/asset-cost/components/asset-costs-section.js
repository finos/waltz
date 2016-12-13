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

import {assetCostKindNames} from '../../common/services/display_names';
import {initialiseData} from '../../common';


const bindings = {
    costView: '<',
    loadAll: '<',
    csvName: '@?',
    sourceDataRatings: '<'
};


const initialState = {
    visibility: {
        summary: true,
        detail: false
    }
};


function processSelection(d) {
    if (!d) return null;

    const costTable = _.map(d.costs, (v, k) => ({ type: assetCostKindNames[k] || k, amount: v }));
    return Object.assign({}, d, { costTable });
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.onSummarySelect = (d) => vm.summarySelection = processSelection(d);

    vm.showSummary = () => {
        vm.visibility.summary = true;
        vm.visibility.detail = false;
    };

    vm.showDetail = () => {
        vm.visibility.summary = false;
        vm.visibility.detail = true;
        vm.loadAll();
    };
}


controller.$inject = [
];


const component = {
    template: require('./asset-costs-section.html'),
    bindings,
    controller
};


export default component;
