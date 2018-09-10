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

import template from "./person-hierarchy-section.html";
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";

const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    cumulativeCount: null,
    kindCount: null,
    kindKeys: null
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        serviceBroker
            .loadViewData(
                CORE_API.PersonStore.getById,
                [ vm.parentEntityRef.id ])
            .then(r => r.data.employeeId)
            .then(empId => {
                serviceBroker
                    .loadViewData(CORE_API.PersonStore.findDirects, [ empId ])
                    .then(r => vm.directs = r.data);

                serviceBroker
                    .loadViewData(CORE_API.PersonStore.findManagers, [ empId ])
                    .then(r => vm.managers = r.data);

                serviceBroker
                    .loadViewData(CORE_API.PersonStore.countCumulativeReportsByKind, [ empId ])
                    .then(r =>{
                        vm.kindKeys = _.keys(r.data);
                        vm.kindCounts = r.data;
                        vm.cumulativeCount = _.sum(_.values(r.data));
                    })
            });
    };
}

controller.$inject = [ "ServiceBroker" ];


const component = {
    bindings,
    template,
    controller
};


const id = "waltzPersonHierarchySection";


export default {
    id,
    component
};