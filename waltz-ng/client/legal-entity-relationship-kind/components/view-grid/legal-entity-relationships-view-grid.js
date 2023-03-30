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
import template from "./legal-entity-relationships-view-grid.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import {initialiseData} from "../../../common";
import {mkSelectionOptions} from "../../../common/selector-utils";

const bindings = {
    parentEntityRef: "<",
    relationshipKindId: "<"
};

const relationshipColDefs = [
    {
        field: "relationship.targetEntityReference",
        name: "Target Entity",
        width: "20%",
        toSearchTerm: d => _.get(d, ["relationship", "targetEntityReference", "name"], ""),
        cellTemplate: `<div style="padding-top: 0.5em">
                            <span ng-bind="COL_FIELD.name"></span>
                       </div>`
    },
    {
        field: "relationship.targetEntityReference",
        name: "Target Entity External Id",
        width: "10%",
        toSearchTerm: d => _.get(d, ["relationship", "targetEntityReference", "externalId"], ""),
        cellTemplate: `<div style="padding-top: 0.5em">
                            <span ng-bind="COL_FIELD.externalId"></span>
                       </div>`
    },
    {
        field: "relationship.legalEntityReference",
        name: "Legal Entity",
        width: "20%",
        toSearchTerm: d => _.get(d, ["relationship", "legalEntityReference", "name"], ""),
        cellTemplate: `<div style="padding-top: 0.5em">
                            <span ng-bind="COL_FIELD.name"></span>
                       </div>`
    },
    {
        field: "relationship.legalEntityReference",
        name: "Legal Entity External Id",
        width: "10%",
        toSearchTerm: d => _.get(d, ["relationship", "legalEntityReference", "externalId"], ""),
        cellTemplate: `<div style="padding-top: 0.5em">
                            <span ng-bind="COL_FIELD.externalId"></span>
                       </div>`
    }
];


const updatedAtColDefs = [
    {
        field: "relationship.lastUpdatedAt",
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
        field: "relationship.lastUpdatedBy",
        name: "Last Updated By",
        width: "10%"
    }
]


const initialState = {
    relationshipKind: null,
    relationships: [],
    stats: null,
    visibility: {
        overlay: false,
        bulkUpload: false,
        loading: true
    },
}


function mkRatingsStringSearch(header, row) {
    const ratingsForDef = row.ratingsByDefId[header.id];
    return _.chain(ratingsForDef)
        .map(r => _.get(r, ["name"], ""))
        .join(" ")
        .value();
}

function controller($q, $scope, $state, serviceBroker) {

    const vm = initialiseData(this, initialState);

    function loadRelationships() {

        const selectionOptions = mkSelectionOptions(vm.parentEntityRef);

        const relKindsPromise = serviceBroker
            .loadViewData(CORE_API.LegalEntityRelationshipKindStore.getById, [vm.relationshipKindId])
            .then(r => vm.relationshipKind = r.data);

        const relKindStatsPromise = serviceBroker
            .loadViewData(CORE_API.LegalEntityRelationshipKindStore.getUsageStatsForKindAndSelector, [vm.relationshipKindId, selectionOptions])
            .then(r => vm.stats = r.data);

        const relationshipsViewPromise = serviceBroker
            .loadViewData(CORE_API.LegalEntityRelationshipStore.getViewByRelationshipKindId,
                [vm.relationshipKindId, selectionOptions],
                {force: true})
            .then(r => r.data);

        const ratingsPromise = serviceBroker
            .loadViewData(CORE_API.RatingSchemeStore.findAllRatingsSchemeItems)
            .then(r => _.keyBy(r.data, d => d.id));


        return $q
            .all([relationshipsViewPromise, ratingsPromise])
            .then(([relationshipsView, ratingSchemeItemsById]) => {

                const assessmentColDefs = _
                    .chain(relationshipsView.assessmentHeaders)
                    .sortBy(d => d.name)
                    .map(d => ({
                        field: `ratingsByDefId[${d.id}]`,
                        name: d.name,
                        width: 200,
                        toSearchTerm: r => mkRatingsStringSearch(d, r),
                        cellTemplate: `
                           <div class="ui-grid-cell-contents"
                                style="vertical-align: baseline;">
                                <ul class="list-inline">
                                <li ng-repeat="c in COL_FIELD">
                                    <waltz-rating-indicator-cell rating="c"
                                                                 show-description-popup="true"
                                                                 show-name="true">
                                    </waltz-rating-indicator-cell>
                                </li>
                                </ul>
                                </span>
                            </div>`
                    }))
                    .value();

                vm.columnDefs = _.concat(relationshipColDefs, assessmentColDefs, updatedAtColDefs);

                vm.relationships = _
                    .chain(relationshipsView.rows)
                    .map(d => {
                        const ratingsByDefId = _
                            .chain(d.assessments)
                            .keyBy(r => r.assessmentDefinitionId)
                            .mapValues(v => _
                                .chain(v.ratingIds)
                                .map(r => ratingSchemeItemsById[r])
                                .filter(d => d != null)
                                .sortBy(r => r.position, r => r.name)
                                .value())
                            .value();

                        return Object.assign(
                            {},
                            d,
                            {
                                ratingsByDefId,
                            })
                    })
                    .sortBy(d => d.relationship.targetEntityReference.name, d => d.relationship.legalEntityReference.name)
                    .value();
            })
            .then(() => vm.visibility.loading = false);
    }

    vm.$onChanges = () => {
        return loadRelationships();
    }

    vm.doneUpload = () => {
        $scope.$applyAsync(() => {
            vm.cancelBulkUpload();
            loadRelationships();
        })
    }

    vm.onRowSelect = (r) => {
        $state.go(
            "main.legal-entity-relationship.view",
            {id: r.relationship.id});
    }
}

controller.$inject = [
    "$q",
    "$scope",
    "$state",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    id: "waltzLegalEntityRelationshipsViewGrid",
    component
};