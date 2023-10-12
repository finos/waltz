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
import template from "./logical-flow-view-grid.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import {initialiseData} from "../../../common";
import {mkSelectionOptions} from "../../../common/selector-utils";

const bindings = {
    parentEntityRef: "<",
};

const flowColDefs = [
    {
        field: "source",
        name: "Source Entity",
        width: "20%",
        toSearchTerm: d => _.get(d, ["source", "name"], ""),
        cellTemplate: `<div style="padding-top: 0.5em">
                            <span ng-bind="COL_FIELD.name"></span>
                       </div>`
    },
    {
        field: "source",
        name: "Source Entity External Id",
        width: "10%",
        toSearchTerm: d => _.get(d, ["source", "externalId"], ""),
        cellTemplate: `<div style="padding-top: 0.5em">
                            <span ng-bind="COL_FIELD.externalId"></span>
                       </div>`
    },
    {
        field: "target",
        name: "Target Entity",
        width: "20%",
        toSearchTerm: d => _.get(d, ["target", "name"], ""),
        cellTemplate: `<div style="padding-top: 0.5em">
                            <span ng-bind="COL_FIELD.name"></span>
                       </div>`
    },
    {
        field: "target",
        name: "Target Entity External Id",
        width: "10%",
        toSearchTerm: d => _.get(d, ["target", "externalId"], ""),
        cellTemplate: `<div style="padding-top: 0.5em">
                            <span ng-bind="COL_FIELD.externalId"></span>
                       </div>`
    },
    {
        field: "dataTypes",
        name: "DataTypes",
        width: "10%",
        toSearchTerm: d => _.get(d, ["dataTypes"], ""),
        cellTemplate: `<div style="padding-top: 0.5em">
                            <span ng-bind="COL_FIELD"></span>
                       </div>`
    }
];


const updatedAtColDefs = [
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


const initialState = {
    rows: [],
    stats: null,
    visibility: {
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

    function loadFlows() {

        const selectionOptions = mkSelectionOptions(vm.parentEntityRef);

        const logicalFlowViewPromise = serviceBroker
            .loadViewData(CORE_API.LogicalFlowStore.getViewForSelector,
                          [selectionOptions],
                          {force: true})
            .then(r => console.log({view: r.data}) || r.data);

        return $q
            .all([logicalFlowViewPromise])
            .then(([logicalFlowView]) => {

                const ratingsByFlowId = _.groupBy(logicalFlowView.flowRatings, d => d.entityReference.id);
                const ratingSchemeItemsById = _.keyBy(logicalFlowView.ratingSchemeItems, d => d.id);
                const decoratorsByFlowId = _.groupBy(logicalFlowView.dataTypeDecorators, d => d.dataFlowId);

                const assessmentColDefs = _
                    .chain(logicalFlowView.primaryAssessmentDefinitions)
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

                console.log({assessmentColDefs});

                vm.columnDefs = _.concat(flowColDefs, assessmentColDefs, updatedAtColDefs);

                vm.rows = _
                    .chain(logicalFlowView.flows)
                    .map(d => {

                        const assessmentRatingsForFlow = _.get(ratingsByFlowId, d.id, []);

                        const dataTypesForFlow = _.get(decoratorsByFlowId, d.id, []);

                        console.log({decoratorsByFlowId, dataTypesForFlow});

                        const dataTypes = _
                            .chain(dataTypesForFlow)
                            .map(d => d.decoratorEntity.name)
                            .join(", ")
                            .value();

                        const ratingsByDefId = _
                            .chain(assessmentRatingsForFlow)
                            .groupBy(r => r.assessmentDefinitionId)
                            .mapValues(v => _
                                .chain(v)
                                .map(r => ratingSchemeItemsById[r.ratingId])
                                .filter(d => d != null)
                                .sortBy(r => r.position, r => r.name)
                                .value())
                            .value();

                        return Object.assign(
                            {},
                            d,
                            {
                                ratingsByDefId,
                                dataTypes
                            })
                    })
                    .sortBy(d => d.target.name, d => d.source.name)
                    .value();

                console.log({rows: vm.rows});
            })
            .then(() => vm.visibility.loading = false);
    }

    vm.$onChanges = () => {
        if (vm.parentEntityRef) {
            loadFlows();
        }
    }

    vm.onRowSelect = (r) => {

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
    id: "waltzLogicalFlowViewGrid",
    component
};