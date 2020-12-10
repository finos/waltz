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
import {loadAssessmentsBySelector} from "../../../assessments/assessment-utils";
import {nest} from "d3-collection";


const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    licenceAssessmentDefinitions: [],
    softwareCatalog: null,
    softwarePackage: null,
    selectedVersion: null,
    selectedApps: [],
    selectedDetails: "NONE", // NONE | VERSIONS | APPS
    selectedLicences: [],
    selectedVulnerabilityCounts: null,
    vulnerabilityCountsByVersionId: {}
};


function mkColumnDefs() {
    return [
        {
            field: "version",
            displayName: "Version",
            cellTemplate: `
                <div class="ui-grid-cell-contents">
                    <a class="clickable"
                              ng-bind="COL_FIELD"
                              ng-click="grid.appScope.onSelectVersion(row.entity)">
                    </a>
                    <waltz-icon ng-if="row.entity.vulnerabilityCounts.High" name="exclamation-circle" style="color: #d62728"></waltz-icon>
                    <waltz-icon ng-if="!row.entity.vulnerabilityCounts.High && row.entity.vulnerabilityCounts.Medium" name="warning" style="color: #ff7f0e"></waltz-icon>
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
                                  ng-click="grid.appScope.onSelectVersionAppCount(row.entity)">
                               </a>
                           </div>`
        }
    ];
}


function mkGridData(softwarePackage = {}, versions = [], usages = [], vulnerabilityCountsByVersionId = {}) {
    const countsByVersionId = countByVersionId(usages);
    const gridData = _.map(versions, v => {
        const countList = _.get(vulnerabilityCountsByVersionId, `[${v.id}].tallies`, []);
        const countMap = _.keyBy(countList, 'id');
        return Object.assign(
            {},
            v,
            {usageCount: _.get(countsByVersionId, `[${v.id}]`, 0) },
            {vulnerabilityCounts: countMap });
    });
    return gridData;
}


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadPackage = () => {
        const catalogPromise = serviceBroker
            .loadViewData(
                CORE_API.SoftwareCatalogStore.getByPackageId,
                [vm.parentEntityRef.id])
            .then(r => r.data);

        const vulnerabilitySeverityPromise = serviceBroker
            .loadViewData(CORE_API.VulnerabilityStore.countSeveritiesBySelector, [mkSelectionOptions(vm.parentEntityRef)])
            .then(r => r.data);

        $q.all([catalogPromise, vulnerabilitySeverityPromise])
            .then(([softwareCatalog, vulnerabilityCounts]) => {
                vm.softwareCatalog = softwareCatalog;
                vm.softwarePackage = _.get(vm.softwareCatalog, "packages[0]");
                const nestedTallies = _.map(vulnerabilityCounts, v => Object.assign({}, v, {tallyMap: nest().key(t => t.id).object(v.tallies) }));
                vm.vulnerabilityCountsByVersionId = _.keyBy(nestedTallies, v => v.entityReference.id);

                vm.columnDefs = mkColumnDefs();
                vm.gridData = mkGridData(vm.softwarePackage,
                                         vm.softwareCatalog.versions,
                                         vm.softwareCatalog.usages,
                                         vm.vulnerabilityCountsByVersionId);
            });
    };


    vm.onSelectVersionAppCount = (version) => {
        vm.selectedVersion = version;
        vm.selectedDetails = "APPS";
        serviceBroker
            .loadViewData(CORE_API.ApplicationStore.findBySelector, [mkSelectionOptions(version)])
            .then(r => r.data)
            .then(apps => {
                vm.selectedApps = apps;
            });
    };

    vm.onSelectVersion = (version) => {
        vm.selectedVersion = version;
        vm.selectedDetails = "VERSIONS";

        vm.selectedVulnerabilityCounts = _.get(vm.vulnerabilityCountsByVersionId, `[${version.id}.tallyMap]`, null);

        // get the licences for this version
        const licencePromise = serviceBroker
            .loadViewData(CORE_API.LicenceStore.findBySelector, [mkSelectionOptions(version)])
            .then(r => r.data);

        $q.all([licencePromise, loadAssessmentsBySelector($q, serviceBroker, "LICENCE", mkSelectionOptions(vm.parentEntityRef), true)])
            .then(([licences, assessments]) => {
                vm.licenceAssessmentDefinitions = assessments.definitions;
                const assessmentsByLicenceId = assessments.assessmentsByEntityId;

                vm.selectedLicences =_.map(
                    licences,
                    l => {
                        const assessmentsByDefinitionExtId = _.get(assessmentsByLicenceId, l.id, []);
                        return Object.assign({}, l, assessmentsByDefinitionExtId)
                    });
            });
    };


    vm.$onInit = () => {
        loadPackage();
    };
}


controller.$inject = [
    "$q",
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
