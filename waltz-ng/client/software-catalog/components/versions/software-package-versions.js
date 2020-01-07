/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019  Waltz open source project
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
import {initialiseData} from "../../../common";

import template from "./software-package-versions.html";


const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    softwareCatalog: null,
    softwarePackage: null
};


function mkColumnDefs() {
    return [
        {
            field: "version",
            name: "Version",
        },
        {
            field: "externalId",
            name: "External Id",
        },
        {
            field: "releaseDate",
            cellTemplate: `
                <waltz-from-now class="text-muted"
                                timestamp="row.entity.releaseDate"
                                days-only="true">
                </waltz-from-now>`
        },
        {
            field: "usageCount",
            name: "# Applications"
        }
    ];
}


function mkGridData(softwarePackage = {}, versions = [], usages = []) {
    const usagesByVersionId = _
        .chain(usages)
        .map(u => Object.assign({}, _.pick(u, ["softwarePackageId", "softwareVersionId", "applicationId"])))
        .uniqWith(_.isEqual)
        .groupBy(u => u.softwareVersionId)
        .value();

    const gridData = _.map(versions, v => Object.assign(
        {},
        v,
        {usageCount: _.get(usagesByVersionId, `[${v.id}].length`, 0)}));

    return gridData;
}



function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadPackage = () => {
        return serviceBroker
            .loadViewData(
                CORE_API.SoftwareCatalogStore.getByPackageId,
                [vm.parentEntityRef.id])
            .then(r => {
                vm.softwareCatalog = r.data;
                vm.softwarePackage = _.get(vm.softwareCatalog, "packages[0]");

                vm.columnDefs = mkColumnDefs();
                vm.gridData = mkGridData(vm.softwarePackage,
                                         vm.softwareCatalog.versions,
                                         vm.softwareCatalog.usages);
            });
    };


    vm.$onInit = () => {
        loadPackage();
    };
}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzSoftwarePackageVersions"
};
