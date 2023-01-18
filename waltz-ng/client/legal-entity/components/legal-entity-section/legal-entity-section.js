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
import template from "./legal-entity-section.html";
import {initialiseData} from "../../../common";
import LegalEntitySection from "./LegalEntitySection.svelte";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";

const bindings = {
    parentEntityRef: "<"
};

const initialState = {
    LegalEntitySection,
    relationshipKinds: [],
    relationships: [],
    visibility: {
        overlay: false
    },
    columnDefs: [
        {
            field: "legalEntityReference",
            name: "Legal Entity",
            width: "40%",
            cellTemplate: `<div style="padding-top: 0.5em">
                                <waltz-entity-link entity-ref="row.entity.legalEntityReference"></waltz-entity-link>
                           </div>`
        },
        {
            field: "relationshipKind.name",
            name: "Relationship",
            width: "15%"
        },
        {field: "description", width: "20%"},
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
            width: "15%"
        }
    ]
}


function controller($q, serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {

        const relKindsPromise = serviceBroker
            .loadAppData(CORE_API.LegalEntityRelationshipKindStore.findAll, [])
            .then(r => r.data);

        const relationshipsPromise = serviceBroker
            .loadViewData(CORE_API.LegalEntityRelationshipStore.findByEntityReference, [vm.parentEntityRef])
            .then(r => r.data);

        return $q
            .all([relKindsPromise, relationshipsPromise])
            .then(([relKinds, relationships]) => {
                const relKindsById = _.keyBy(relKinds, d => d.id);

                vm.relationships = _.map(
                    relationships,
                    d => Object.assign({}, d, {relationshipKind: relKindsById[d.relationshipKindId]}));
            });
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
    id: "waltzLegalEntitySection",
    component
};