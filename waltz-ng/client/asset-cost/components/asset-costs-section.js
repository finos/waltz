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

import _ from 'lodash';
import {initialiseData} from '../../common';
import {CORE_API} from '../../common/services/core-api-utils';
import template from './asset-costs-section.html';
import {mkSelectionOptions} from "../../common/selector-utils";

const bindings = {
    parentEntityRef: '<',
    csvName: '@?',
};


const initialState = {
    scope: 'CHILDREN',
    csvName: 'asset-costs.csv',
    visibility: {
        summary: true,
        detail: false
    }
};


function processSelection(displayNameService, d) {
    if (!d) return null;

    const costTable = _.map(d.costs, (v, k) => ({ type: displayNameService.lookup("CostKind", k) || k, amount: v }));
    return Object.assign({}, d, { costTable });
}


function controller(serviceBroker,
                    displayNameService) {

    const vm = initialiseData(this, initialState);

    vm.onSummarySelect = (d) => vm.summarySelection = processSelection(displayNameService, d);

    vm.showSummary = () => {
        vm.visibility.summary = true;
        vm.visibility.detail = false;
    };

    vm.showDetail = () => {
        vm.visibility.summary = false;
        vm.visibility.detail = true;

        serviceBroker
            .loadViewData(
                CORE_API.AssetCostStore.findAppCostsByAppIdSelector,
                [ mkSelectionOptions(vm.parentEntityRef) ])
            .then(r => vm.allCosts = r.data);
    };

    vm.$onInit = () => {
        serviceBroker
            .loadViewData(
                CORE_API.AssetCostStore.findTopAppCostsByAppIdSelector,
                [ mkSelectionOptions(vm.parentEntityRef) ])
            .then(r => vm.topCosts = r.data);

    };
}


controller.$inject = [
    'ServiceBroker',
    'DisplayNameService'
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzAssetCostsSection'
};
