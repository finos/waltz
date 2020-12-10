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
import {CORE_API} from "../../../common/services/core-api-utils";

import {initialiseData} from "../../../common";
import {mkEntityLinkGridCell, mkLinkGridCell} from "../../../common/grid-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {countByVersionId} from "../../software-catalog-utilities";

import template from "./software-packages-section.html";


const bindings = {
    parentEntityRef: "<",
};


const initialState = {
    softwareCatalog: null,
    selectedPackage: null,
    selectedVersion: null
};


function mkColumnDefs() {
    return [
        mkLinkGridCell("Name",
                       "package.name",
                       "package.id",
                       "main.software-package.view"),
        { field: "version.externalId", displayName: "External Id" },
        mkEntityLinkGridCell("Version", "version", "none", "right"),
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
        {
            field: "usageCount",
            name: "# Applications",
            cellTemplate: `<div class="ui-grid-cell-contents">
                               <a class="clickable"
                                  ng-bind="COL_FIELD"
                                  ng-click="grid.appScope.onSelectVersion(row.entity)">
                               </a>
                           </div>`
        }
    ]
}


function mkGridData(packages = [], versions = [], usages = []) {
    const countsByVersionId = countByVersionId(usages);
    const packagesById = _.keyBy(packages, v => v.id);

    const gridData = _.map(versions, v => Object.assign(
        { },
        { package: packagesById[v.softwarePackageId] },
        { version: v },
        { usageCount: _.get(countsByVersionId, `[${v.id}]`, 0) }));

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
                vm.gridData = mkGridData(
                    vm.softwareCatalog.packages,
                    vm.softwareCatalog.versions,
                    vm.softwareCatalog.usages);
            });
    };


    vm.onSelectVersion = (row) => {
        vm.selectedPackage = row.package;
        vm.selectedVersion = row.version;
        serviceBroker
            .loadViewData(CORE_API.ApplicationStore.findBySelector, [mkSelectionOptions(vm.selectedVersion)])
            .then(r => r.data)
            .then(apps => {
                vm.selectedApps = apps;
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
