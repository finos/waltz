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
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkLinkGridCell} from "../../../common/grid-utils";
import {loadAssessmentsForKind} from "../../../assessments/assessment-utils";

import template from "./licence-list.html";


const bindings = {
};


const nameCol = mkLinkGridCell(
    "Name",
    "name",
    "id",
    "main.licence.view",
    { width: "50%" });


const assessmentsCol = {
    field: "assessments",
    name: "Assessments",
    width: "7%",
    cellTemplate: `
                <div style="text-align: center">
                    <waltz-assessment-rating-traffic-lights assessments="COL_FIELD">
                    </waltz-assessment-rating-traffic-lights>
                </div>`
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
    columnDefs: []
};


function mkColumnDefs(assessmentDefs) {
    const assessmentFields = _.map(assessmentDefs, d => {
        return {
            field: `${d.externalId}.ratings[0].ratingItem.name`,
            displayName: d.name
        };
    });

    return _.union(
        [
            nameCol,
            externalIdCol,
        ],
        assessmentFields,
        [
            usageCol,
            lastUpdatedCol
        ]
    );
}


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const licencePromise = serviceBroker
            .loadViewData(CORE_API.LicenceStore.findAll)
            .then(r => r.data);

        const usagePromise = serviceBroker
            .loadViewData(CORE_API.LicenceStore.countApplications)
            .then(r => _.keyBy(r.data, "id"));

        const assessmentsPromise = loadAssessmentsForKind($q, serviceBroker, "LICENCE");

        $q.all([licencePromise, usagePromise, assessmentsPromise])
            .then(([licences, usageByLicenseId, assessments]) => {
                const assessmentDefinitions = assessments.definitions;
                const assessmentsByLicenceId = assessments.assessmentsByEntityId;

                vm.columnDefs = mkColumnDefs(assessmentDefinitions);

                vm.licences = _.map(licences, licence => {
                    const usageCount = _.get(usageByLicenseId, [licence.id, "count"], 0);
                    const usageInfo = usageCount > 0
                        ? `${usageCount} Applications`
                        : "-";

                    const assessmentsByDefinitionExtId = _.get(
                        assessmentsByLicenceId,
                        licence.id,
                        []);

                    return Object.assign({}, licence, { usageInfo }, assessmentsByDefinitionExtId)
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

