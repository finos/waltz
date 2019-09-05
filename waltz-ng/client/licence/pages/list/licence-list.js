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
import { CORE_API } from "../../../common/services/core-api-utils";

import template from "./licence-list.html";
import { mkLinkGridCell } from "../../../common/grid-utils";


const bindings = {
};


const nameCol = mkLinkGridCell(
    "Name",
    "name",
    "id",
    "main.licence.view",
    { width: "50%" });

const approvalCol = {
    field: "approvalStatus",
    name: "Status",
    cellFilter: "toDisplayName:'ApprovalStatus'"
};

const externalIdCol = {
    field: "externalId",
    name: "External Id",
};


const usageCol = {
    field: "usageInfo",
    name: "# Applications"
};


const lastUpdatedCol = {
    field: "lastUpdated",
    cellTemplate: `
        <waltz-last-updated entity="row.entity"
                            show-label="false">
        </waltz-last-updated>`
};


const initialState = {
    licences: [],
    columnDefs: [
        nameCol,
        externalIdCol,
        approvalCol,
        usageCol,
        lastUpdatedCol
    ]
};


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const licencePromise = serviceBroker
            .loadViewData(CORE_API.LicenceStore.findAll)
            .then(r => r.data);

        const usagePromise = serviceBroker
            .loadViewData(CORE_API.LicenceStore.countApplications)
            .then(r => _.keyBy(r.data, "id"));

        $q.all([licencePromise, usagePromise])
            .then(([licences, usageByLicenseId]) => {
                vm.licences = _.map(licences, d => {
                    const usageCount = _.get(usageByLicenseId, [d.id, "count"], 0);
                    const usageInfo = usageCount > 0
                        ? `${usageCount} Applications`
                        : "-";

                    return Object.assign({}, d, { usageInfo })
                });
            });
    };


    vm.onGridInitialised = (api) => {
        vm.exportLicences = () => api.exportFn("licences.csv");
    };
}


controller.$inject = [
    "$q",
    "ServiceBroker"
];


const component = {
    bindings,
    controller,
    template
};


export default {
    id: "waltzLicenceList",
    component
};

