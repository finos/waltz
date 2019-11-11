/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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
import { initialiseData } from "../../../common";
import {mkSelectionOptions} from "../../../common/selector-utils";
import { CORE_API } from "../../../common/services/core-api-utils";
import { hierarchyQueryScope } from "../../../common/services/enums/hierarchy-query-scope";
import { entityLifecycleStatus } from "../../../common/services/enums/entity-lifecycle-status";

import template from "./simple-app-table.html";


const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    apps: []
};


function controller($scope,
                    serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const selectorOptions = mkSelectionOptions(
            vm.parentEntityRef,
            "EXACT",
            [entityLifecycleStatus.ACTIVE.key],
            vm.filters);

        serviceBroker
            .loadViewData(CORE_API.ApplicationStore.findBySelector, [selectorOptions])
            .then(r => vm.apps = r.data);
    };

    vm.$onChanges = (changes) => {
        console.log('==========> onChnages');
    };

    vm.onInitialise = (cfg) => {
        vm.export = () => cfg.exportFn("apps.csv");
    };
}


controller.$inject = [
    "$scope",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default component;