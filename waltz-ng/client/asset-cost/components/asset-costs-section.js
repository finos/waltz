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
import {assetCostKindNames} from '../../common/services/display-names';
import {initialiseData} from '../../common';
import {CORE_API} from '../../common/services/core-api-utils';
import template from './asset-costs-section.html';

const bindings = {
    parentEntityRef: '<',
    scope: '@?',
    csvName: '@?',
};


const initialState = {
    scope: 'CHILDREN',
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


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    let selector = null;

    vm.onSummarySelect = (d) => vm.summarySelection = processSelection(d);

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
                [ selector ])
            .then(r => vm.allCosts = r.data);

    };

    vm.$onInit = () => {
        selector = {
            entityReference: vm.parentEntityRef,
            scope: vm.scope
        };

        serviceBroker
            .loadViewData(
                CORE_API.AssetCostStore.findTopAppCostsByAppIdSelector,
                [ selector ])
            .then(r => vm.topCosts = r.data);

        serviceBroker
            .loadAppData(
                CORE_API.StaticPanelStore.findByGroup,
                ['SECTION.ASSET_COSTS.ABOUT'])
            .then(rs => vm.staticPanels = rs.data);
    };
}


controller.$inject = [
    'ServiceBroker'
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
