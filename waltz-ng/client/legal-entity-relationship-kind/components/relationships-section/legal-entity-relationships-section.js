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
import template from "./legal-entity-relationships-section.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import {initialiseData} from "../../../common";

const bindings = {
    parentEntityRef: "<"
};

const initialState = {
    relationshipKind: null,
    relationships: [],
    visibility: {
        overlay: false,
        bulkUpload: false
    },
    columnDefs: [
        {
            field: "targetEntityReference",
            name: "Target Entity",
            width: "25%",
            cellTemplate: `<div style="padding-top: 0.5em">
                                <waltz-entity-link entity-ref="row.entity.targetEntityReference"></waltz-entity-link>
                           </div>`
        },
        {
            field: "legalEntityReference",
            name: "Legal Entity",
            width: "25%",
            cellTemplate: `<div style="padding-top: 0.5em">
                                <waltz-entity-link entity-ref="row.entity.legalEntityReference"></waltz-entity-link>
                           </div>`
        },
        {field: "description", width: "30%"},
        {
            field: "lastUpdatedAt",
            name: "Last Updated At",
            width: "10%",
            cellTemplate: `
                   <div class="ui-grid-cell-contents"
                        style="vertical-align: baseline;">
                        <waltz-from-now timestamp="COL_FIELD"
                                        days-only="true">
                        </waltz-from-now>
                    </div>`
        },
        {
            field: "lastUpdatedBy",
            name: "Last Updated By",
            width: "10%"
        }
    ]
}


function controller($q, serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {

        const relKindsPromise = serviceBroker
            .loadViewData(CORE_API.LegalEntityRelationshipKindStore.getById, [vm.parentEntityRef.id])
            .then(r => r.data);

        const relationshipsPromise = serviceBroker
            .loadViewData(CORE_API.LegalEntityRelationshipStore.findByRelationshipKindId, [vm.parentEntityRef.id])
            .then(r => r.data);

        return $q
            .all([relKindsPromise, relationshipsPromise])
            .then(([relKind, relationships]) => {
                vm.relationshipKind = relKind;

                vm.relationships = _.sortBy(
                    relationships,
                    d => d.targetEntityReference.name, d => d.legalEntityReference.name);
            });
    }

    vm.bulkUpload = () => {
        vm.visibility.bulkUpload = true;
    }

    vm.cancelBulkUpload = () => {
        vm.visibility.bulkUpload = false;
    }
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
    id: "waltzLegalEntityRelationshipsSection",
    component
};