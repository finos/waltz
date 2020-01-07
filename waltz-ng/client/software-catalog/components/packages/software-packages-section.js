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
import {mkLinkGridCell} from "../../../common/grid-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";

import template from "./software-packages-section.html";


const bindings = {
    parentEntityRef: "<",
};


const initialState = {
    softwareCatalog: null,
};


function mkColumnDefs() {
    return [
        mkLinkGridCell("Name",
            "package.name",
            "package.id",
            "main.software-package.view"),
        { field: "version.externalId", displayName: "External Id" },
        { field: "version.version", displayName: "Version"},
        {
            field: "releaseDate",
            cellTemplate: `
                <waltz-from-now class="text-muted"
                                timestamp="row.entity.version.releaseDate"
                                days-only="true">
                </waltz-from-now>`
        },
        { field: "package.description", displayName: "Description" },
        { field: "package.isNotable", displayName: "Notable" },
        { field: "usageCount", name: "# Applications" }
    ]
}


function mkGridData(packages = [], versions = [], usages = []) {
    const usagesByVersionId = _
        .chain(usages)
        .map(u => Object.assign({}, _.pick(u, ["softwarePackageId", "softwareVersionId", "applicationId"])))
        .uniqWith(_.isEqual)
        .groupBy(u => u.softwareVersionId)
        .value();

    const versionsById = _.keyBy(versions, v => v.id);
    const packagesById = _.keyBy(packages, v => v.id);

    const gridData = _.map(usages, u => Object.assign(
        { },
        { package: packagesById[u.softwarePackageId] },
        { version: versionsById[u.softwareVersionId] },
        { usageCount: _.get(usagesByVersionId, `[${u.softwareVersionId}].length`, 0) })
    );

    return gridData;
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadPackage = () => {
        const options = mkSelectionOptions(vm.parentEntityRef);
        return serviceBroker
            .loadViewData(
                CORE_API.SoftwareCatalogStore.findBySelector,
                [options])
            .then(r => {
                vm.softwareCatalog = r.data;
                vm.columnDefs = mkColumnDefs();
                vm.gridData = mkGridData(vm.softwareCatalog.packages,
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
    id: "waltzSoftwarePackagesSection"
};
