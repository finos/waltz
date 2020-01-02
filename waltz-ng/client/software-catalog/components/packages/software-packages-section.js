/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import _ from "lodash";
import { CORE_API } from "../../../common/services/core-api-utils";

import { initialiseData } from "../../../common";
import { mkLinkGridCell } from "../../../common/grid-utils";
import { mkSelectionOptions } from "../../../common/selector-utils";

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
    const usagesByVersionId = _.groupBy(usages, "softwareVersionId");

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
