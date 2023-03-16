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
import {inputString, activeMode, Modes} from "../bulk-upload/bulk-upload-relationships-store";

const bindings = {
    parentEntityRef: "<"
};

const relationshipColDefs = [
    {
        field: "relationship.targetEntityReference",
        name: "Target Entity",
        width: "20%",
        cellTemplate: `<div style="padding-top: 0.5em">
                            <span>{row.entity.relationship.targetEntityReference.name}</span>
                       </div>`
    },
    {
        field: "relationship.legalEntityReference",
        name: "Legal Entity",
        width: "20%",
        cellTemplate: `<div style="padding-top: 0.5em">
                            <span>{row.entity.relationship.legalEntityReference.name}</span>
                       </div>`
    },
    {
        field: "relationship.description"
    },
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
];


const initialState = {
    relationshipKind: null,
    relationships: [],
    visibility: {
        overlay: false,
        bulkUpload: false
    },
}


function controller($q, $scope, $state, serviceBroker) {

    const vm = initialiseData(this, initialState);

    function loadRelationships() {

        const relKindsPromise = serviceBroker
            .loadViewData(CORE_API.LegalEntityRelationshipKindStore.getById, [vm.parentEntityRef.id])
            .then(r => r.data);


        const relationshipsViewPromise = serviceBroker
            .loadViewData(CORE_API.LegalEntityRelationshipStore.getViewByRelationshipKindId, [vm.parentEntityRef.id], {force: true})
            .then(r => r.data);

        return $q
            .all([relKindsPromise, relationshipsViewPromise])
            .then(([relKind, relationshipsView]) => {
                vm.relationshipKind = relKind;

                const assessmentColDefs = _
                    .chain(relationshipsView.assessmentHeaders)
                    .sortBy(d => d.name)
                    .map(d => ({
                        field: `ratingsByDefId[${d.id}]`,
                        name: d.name,
                        width: 200,
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

                vm.columnDefs = _.concat(relationshipColDefs, assessmentColDefs);

                vm.relationships = _
                    .chain(relationshipsView.rows)
                    .map(d => Object.assign(
                        {},
                        d,
                        {
                            ratingsByDefId: _
                                .chain(d.assessments)
                                .keyBy(d => d.assessmentDefinitionId)
                                .mapValues(d => _.sortBy(d.ratings, d => d.position, d => d.name))
                                .value()
                        }))
                    .sortBy(d => d.relationship.targetEntityReference.name, d => d.relationship.legalEntityReference.name)
                    .value();
            });
    }

    vm.$onChanges = () => {
        return loadRelationships();
    }

    vm.bulkUpload = () => {
        vm.visibility.bulkUpload = true;
    }

    vm.cancelBulkUpload = () => {
        inputString.set(null);
        activeMode.set(Modes.INPUT);
        vm.visibility.bulkUpload = false;
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
    id: "waltzLegalEntityRelationshipsSection",
    component
};