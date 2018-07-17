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
import {mkSelectionOptions} from "../../../common/selector-utils";
import {determineStatMethod} from "../../logical-flow-utils";
import template from './logical-flows-tabgroup.html';


const bindings = {
    parentEntityRef: '<'
};

const initialState = {
    flows: [],
    decorators: [],
    visibility: {
        loadingFlows: false,
        loadingStats: false
    }
};

function controller($q,
                    serviceBroker) {

    const vm = _.defaultsDeep(this, initialState);

    const loadDetail = () => {
        vm.visibility.loadingFlows = true;

        const flowPromise = serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.findBySelector,
                [ vm.selector ])
            .then(r => vm.flows = r.data);

        const decoratorPromise = serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowDecoratorStore.findBySelector,
                [ vm.selector ])
            .then(r => {
                vm.decorators = r.data;
            });

        return $q
            .all([flowPromise, decoratorPromise])
            .then(() => {
                vm.visibility.loadingFlows = false;
            });
    };

    const loadStats = () => {
        vm.loadingStats = true;
        serviceBroker
            .loadViewData(
                determineStatMethod(vm.parentEntityRef.kind),
                [ vm.selector ])
            .then(r => {
                vm.loadingStats = false;
                vm.stats = r.data;
            });
    };

    vm.tabSelected = (tabName, index) => {
        if(index > 0) {
            loadDetail();
        }
        if(index === 1) {
            vm.visibility.boingyEverShown = true;
        }
        vm.currentTabIndex = index;
    };

    vm.$onChanges = () => {
        if (vm.parentEntityRef) {
            vm.selector = mkSelectionOptions(vm.parentEntityRef);
            loadStats();
        }
    };

}


controller.$inject = [
    '$q',
    'ServiceBroker'
];


const component = {
    controller,
    bindings,
    template
};


export default component;
