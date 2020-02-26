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
import {countByVersionId} from "../../software-catalog-utilities";
import {mkSelectionOptions} from "../../../common/selector-utils";

import template from "./software-package-versions.html";


const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    softwareCatalog: null,
    softwarePackage: null,
    selectedVersion: null
};


function mkColumnDefs() {
    return [
        {
            field: "version",
            displayName: "Version",
            cellTemplate: `
                <div class="ui-grid-cell-contents">
                    <waltz-entity-link entity-ref="row.entity"
                                       tooltip-placement="right"
                                       icon-placement="none">
                    </waltz-entity-link>
                </div>`
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
            name: "# Applications",
            cellTemplate: `<div class="ui-grid-cell-contents">
                               <a class="clickable"
                                  ng-bind="COL_FIELD"
                                  ng-click="grid.appScope.onSelectVersion(row.entity)">
                               </a>
                           </div>`
        }
    ];
}


function mkGridData(softwarePackage = {}, versions = [], usages = []) {
    const countsByVersionId = countByVersionId(usages);
    const gridData = _.map(versions, v => Object.assign(
        {},
        v,
        {usageCount: _.get(countsByVersionId, `[${v.id}]`, 0) }));
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


    vm.onSelectVersion = (version) => {
        vm.selectedVersion = version;
        serviceBroker
            .loadViewData(CORE_API.ApplicationStore.findBySelector, [mkSelectionOptions(version)])
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
    id: "waltzSoftwarePackageVersions"
};
