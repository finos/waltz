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
import {mkEntityLabelGridCell} from "../../../common/grid-utils";
import template from "./flow-classification-rules-table.html";


const bindings = {
    parentEntityRef: "<",
    rules: "<?",
    onSelect: "<?"
};


const initialState = {
    consumersByAuthSourceId: {},
    columnDefs: null,
    onSelect: () => console.log("Default onSelect for flow classification rule table")
};




function shouldShowConsumers(parentRef) {
    const kind = _.get(parentRef, "kind", "");
    return kind === "DATA_TYPE";
}


function mkColumnDefs(parentRef, assessmentDefinitions = []) {
    const consumerCell = shouldShowConsumers(parentRef)
        ? {
            field: "consumers",
            displayName: "Consumers",
            cellTemplate: `
            <div class="ui-grid-cell-contents">
                <span class="label"
                      style="cursor: pointer"
                      ng-class="{ 'label-warning': COL_FIELD.length, 'label-default': COL_FIELD.length == 0 }"
                      uib-popover-template="'wast/consumers-popup.html'"
                      popover-class="waltz-popover-wide"
                      popover-append-to-body="true"
                      popover-placement="top-right"
                      popover-trigger="outsideClick"
                      ng-bind="COL_FIELD.length > 0
                            ? COL_FIELD.length
                            : '-'">
                </span>
            </div>`}
        : null;

    const notesCell = {
        field: "description",
        displayName: "Notes",
        cellTemplate: `
            <div class="ui-grid-cell-contents">
                <span ng-if="COL_FIELD.length > 0">
                    <waltz-icon name="sticky-note"
                                style="color: #337ab7; cursor: pointer"
                                uib-popover-template="'wast/desc-popup.html'"
                                popover-class="waltz-popover-wide"
                                popover-append-to-body="true"
                                popover-placement="top-right"
                                popover-trigger="outsideClick">
                    </waltz-icon>
                </span>
            </div>`
    };

    const classificationCell = {
        field: "classification",
        displayName: "Classification",
        toSearchTerm: d => _.get(d, ["classification", "name"], ""),
        cellTemplate: `
            <div class="ui-grid-cell-contents">
                <div style="display: inline-block;
                            height: 1em;
                            width: 1em;
                            border-radius: 2px;
                            background-color: {{COL_FIELD.color}}">
                </div>
                <span ng-bind="COL_FIELD.name"
                      title="{{COL_FIELD.description}}">
                </span>
            </div>`
    };

    const definitionCells = _.map(assessmentDefinitions, def => {
        return {
            field: `assessmentRatings.${def.id}`,
            displayName: def.name,
            toSearchTerm: d => _.get(d, ["assessmentRatings", def.id, "ratingSchemeItem", "name"], ""),
            cellTemplate: `
                <div class="ui-grid-cell-contents" ng-style="{'background-color': COL_FIELD.ratingSchemeItem.color}">
                    <span ng-bind="COL_FIELD.ratingSchemeItem.name"
                          title="{{COL_FIELD.rating.comment}}">
                    </span>
                </div>
            `
        };
    });

    return _.compact([
        mkEntityLabelGridCell("Data Type", "dataType", "none", "right"),
        mkEntityLabelGridCell("Scope", "vantagePointReference", "left"),
        mkEntityLabelGridCell("Subject", "subjectReference", "left", "right"),
        consumerCell,
        classificationCell,
        ...definitionCells,
        notesCell
    ]);
}


function controller($q, $state, serviceBroker) {

    const vm = initialiseData(this, initialState);


    function loadConsumers() {
        const selector = {
            entityReference: vm.parentEntityRef,
            scope: "CHILDREN"
        };

        return serviceBroker
            .loadViewData(
                CORE_API.FlowClassificationRuleStore.calculateConsumersForDataTypeIdSelector,
                [ selector ])
            .then(r => {
                vm.consumersByAuthSourceId = _
                    .chain(r.data)
                    .keyBy(d => d.key.id)
                    .mapValues(v => _.sortBy(v.value, "name"))
                    .value();
            });
    }


    function mkGridData() {
        const dataTypesById = _.keyBy(vm.dataTypes, "id");

        const ratingSchemeItemsById = _.keyBy(vm.rules.ratingSchemeItems, d => d.id);
        const ratingsByEntityId = _.chain(vm.rules.assessmentRatings)
            .map(r => Object.assign({}, {rating: r, ratingSchemeItem: ratingSchemeItemsById[r.ratingId]}))
            .groupBy(d => d.rating.entityReference.id)
            .value();

        vm.columnDefs = mkColumnDefs(vm.parentEntityRef, vm.rules.primaryAssessmentDefinitions);
        vm.gridData = _.map(vm.rules.flowClassificationRules, d => {
            return {
                id: d.id,
                subjectReference: d.subjectReference,
                dataType: dataTypesById[d.dataTypeId],
                subjectOrgUnitReference: d.subjectOrgUnitReference,
                vantagePointReference: d.vantagePointReference,
                description: d.description,
                classification: vm.classificationsById[d.classificationId],
                consumers: vm.consumersByAuthSourceId[d.id] || [],
                isReadonly: d.isReadonly,
                assessmentRatings: _.keyBy(ratingsByEntityId[d.id] || [], r => r.rating.assessmentDefinitionId)
            };
        });
        console.log(vm.gridData[14])
    }


    function loadAll() {
        const classificationsPromise = serviceBroker
            .loadAppData(CORE_API.FlowClassificationStore.findAll)
            .then(r => vm.classificationsById = _.keyBy(r.data, d => d.id));

        const dataTypePromise = serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => vm.dataTypes = r.data);

        const orgUnitPromise = serviceBroker
            .loadAppData(CORE_API.OrgUnitStore.findAll)
            .then(r => vm.orgUnits = r.data);

        const consumerPromise = shouldShowConsumers(vm.parentEntityRef)
            ? loadConsumers()
            : null;

        return $q
            .all(_.compact([classificationsPromise, dataTypePromise, orgUnitPromise, consumerPromise]))
            .then(mkGridData);
    }

    vm.$onChanges = () => {
        if(vm.rules) {
            loadAll();
        }
    };

    vm.onSelect = (d) => $state.go(
        "main.flow-classification-rule.view",
        { id: d.id });
}


controller.$inject = [
    "$q",
    "$state",
    "ServiceBroker",
    "EnumValueService"
];


export const component = {
    bindings,
    controller,
    template
};

export const id = "waltzFlowClassificationRulesTable";

