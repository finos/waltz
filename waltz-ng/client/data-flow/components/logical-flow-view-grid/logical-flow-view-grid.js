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
import {sameRef} from "../../../common/entity-utils";
import {reduceToSelectedNodesOnly} from "../../../common/hierarchy-utils";

const bindings = {
    parentEntityRef: "<",
};

const flowColDefs = [
    {
        field: "source",
        name: "Source Entity",
        width: "15%",
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
        width: "15%",
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
        field: "dataTypeString",
        name: "DataTypes",
        width: "10%",
        toSearchTerm: d => _.get(d, ["dataTypeString"], ""),
        cellTemplate: `<div style="padding-top: 0.5em">
                            <span ng-bind="COL_FIELD"></span>
                       </div>`
    },
    {
        field: "dataTypeString",
        name: "DataTypes",
        width: "15%",
        toSearchTerm: d => _.get(d, ["dataTypeString"], ""),
        cellTemplate: `<div style="padding-top: 0.5em"
                              uib-popover-template="'wlfvg-data-type-popover.html'"
                              popover-placement="top"
                              popover-trigger="mouseenter"
                              popover-popup-delay="300"
                              popover-class="waltz-popover-width-500"
                              popover-append-to-body="true">
                            <span ng-bind="COL_FIELD | truncate:20"></span>
                       </div>`
    },
    {
        field: "physicalFlows",
        name: "Physical Flow Count",
        width: "10%",
        cellTemplate: `<div style="padding-top: 0.5em; padding-right: 0.5em"
                            class="pull-right">
                            <span ng-bind="COL_FIELD.length"></span>
                       </div>`
    }
];


const physicalFlowColDefs = [
    {
        field: "source",
        name: "Source Entity",
        width: "15%",
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
        width: "15%",
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
        field: "name",
        name: "Name",
        width: "10%",
        cellTemplate: `<div style="padding-top: 0.5em">
                            <span ng-bind="COL_FIELD"></span>
                       </div>`
    },
    {
        field: "externalId",
        name: "External Id",
        width: "10%",
        cellTemplate: `<div style="padding-top: 0.5em">
                            <span ng-bind="COL_FIELD"></span>
                       </div>`
    },
    {
        field: "criticality",
        name: "Criticality",
        width: "10%",
        cellTemplate: `<div style="padding-top: 0.5em">
                            <waltz-enum-value type="'physicalFlowCriticality'"
                                          key="COL_FIELD"
                                          show-icon="false"
                                          show-popover="false">
                            </waltz-enum-value>
                       </div>`
    },
    {
        field: "frequency",
        name: "Frequency",
        width: "10%",
        cellTemplate: `<div style="padding-top: 0.5em">
                            <waltz-enum-value type="'Frequency'"
                                          key="COL_FIELD"
                                          show-icon="false"
                                          show-popover="false">
                            </waltz-enum-value>
                       </div>`
    },
    {
        field: "transport",
        name: "Transport Kind",
        width: "10%",
        cellTemplate: `<div style="padding-top: 0.5em">
                           <span ng-bind="COL_FIELD | toDisplayName:'TransportKind'"></span>
                       </div>`
    }
];


const initialState = {
    rows: [],
    stats: null,
    visibility: {
        loading: true
    },
    selectedFlow: null,
    selectedPhysicalFlow: null,
    logicalFlowUrl: null,
    physicalFlowUrl: null,
    physicalFlowColDefs,
    selectionOptions: null,
    physicalFlowsByDirection: {
        ALL: [],
        UPSTREAM: [],
        DOWNSTREAM: []
    },
    filteredDataTypes: []
}


function mkRatingsStringSearch(header, row) {
    const ratingsForDef = row.ratingsByDefId[header.id];
    return _.chain(ratingsForDef)
        .map(r => _.get(r, ["name"], ""))
        .join(" ")
        .value();
}

function filterFlowOnDataTypes(flows, dataTypes) {
    return _.filter(
        flows,
        d => {
            const dtIds = _.map(d.dataTypes, d => d.dataTypeId);
            return _.some(dataTypes, dt => _.includes(dtIds, dt));
        })
}

function controller($q, $scope, $state, serviceBroker) {

    const vm = initialiseData(this, initialState);

    serviceBroker
        .loadViewData(CORE_API.FlowClassificationStore.findAll)
        .then(r => vm.flowClassificationsByCode = _.keyBy(r.data, d => d.code));


    function loadFlows() {

        const logicalFlowViewPromise = serviceBroker
            .loadViewData(CORE_API.LogicalFlowStore.getViewForSelector,
                          [vm.selectionOptions],
                          {force: true})
            .then(r => r.data);

        const dataTypesPromise = serviceBroker
            .loadViewData(CORE_API.DataTypeStore.findAll)
            .then(r => r.data);

        return $q
            .all([logicalFlowViewPromise, dataTypesPromise])
            .then(([logicalFlowView, allDataTypes]) => {

                vm.allDataTypes = allDataTypes;

                const ratingsByFlowId = _.groupBy(logicalFlowView.flowRatings, d => d.entityReference.id);
                const ratingSchemeItemsById = _.keyBy(logicalFlowView.ratingSchemeItems, d => d.id);
                const decoratorsByFlowId = _.groupBy(logicalFlowView.dataTypeDecorators, d => d.dataFlowId);
                const specsById = _.keyBy(logicalFlowView.physicalSpecifications, d => d.id);

                const physicalsByLogicalFlowId = _
                    .chain(logicalFlowView.physicalFlows)
                    .map(d => Object.assign({}, d, { physicalSpecification: _.get(specsById, d.specificationId)}))
                    .groupBy(d => d.logicalFlowId)
                    .value();

                const dataTypes = _
                    .chain(logicalFlowView.dataTypeDecorators, d => d.decoratorEntity)
                    .uniq()
                    .orderBy(d => d.name)
                    .value();

                vm.mappedDataTypes = dataTypes;
                vm.filteredDataTypes = _.map(dataTypes, d => d.dataTypeId);
                vm.dataTypes = reduceToSelectedNodesOnly(allDataTypes, vm.filteredDataTypes);
                vm.definitionsById = _.keyBy(logicalFlowView.primaryAssessmentDefinitions, d => d.id);

                vm.disableNode = (node) => !_.includes(_.map(dataTypes, dt => dt.dataTypeId), node.id);

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

                vm.columnDefs = _.concat(flowColDefs, assessmentColDefs);

                vm.allLogicalFlows = _
                    .chain(logicalFlowView.flows)
                    .map(d => {

                        const assessmentRatingsForFlow = _.get(ratingsByFlowId, d.id, []);
                        const dataTypes = _.get(decoratorsByFlowId, d.id, []);
                        const physicalFlows = _.get(physicalsByLogicalFlowId, d.id, []);

                        const dataTypeString = _
                            .chain(dataTypes)
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
                                dataTypes,
                                dataTypeString,
                                physicalFlows
                            })
                    })
                    .sortBy(d => d.target.name, d => d.source.name)
                    .value();

                const allPhysicalFlows = _
                    .chain(vm.allLogicalFlows)
                    .flatMap(r => _.map(r.physicalFlows, d => Object.assign({}, d, {source: r.source, target: r.target})))
                    .value();

                vm.physicalFlowsByDirection.UPSTREAM = _.filter(allPhysicalFlows, d => sameRef(d.target, vm.parentEntityRef));
                vm.physicalFlowsByDirection.DOWNSTREAM = _.filter(allPhysicalFlows, d => sameRef(d.source, vm.parentEntityRef));
                vm.physicalFlowsByDirection.ALL = allPhysicalFlows;

                vm.physicalRows = vm.physicalFlowsByDirection.ALL;
                vm.logicalRows = vm.allLogicalFlows;

            })
            .then(() => vm.visibility.loading = false);
    }

    vm.$onChanges = () => {
        if (vm.parentEntityRef) {
            vm.selectionOptions = mkSelectionOptions(vm.parentEntityRef);
            loadFlows();
        }
    }

    vm.onLogicalRowSelect = (r) => {
        if(!_.isNil(r)){
            vm.selectedFlow = r;
            vm.logicalFlowUrl = $state.href("main.logical-flow.view", { id: vm.selectedFlow.id });
            vm.physicalRows = _.filter(vm.physicalFlowsByDirection.ALL, d => d.logicalFlowId === r?.id);
        }
    }

    vm.onPhysicalRowSelect = (r) => {
        if(!_.isNil(r)){
            vm.selectedPhysicalFlow = r;
            vm.physicalFlowUrl = $state.href("main.physical-flow.view", { id: vm.selectedPhysicalFlow.id });
        }
    }

    vm.onClearSelect = () => {
        vm.selectedFlow = null;
        vm.selectedPhysicalFlow = null;
        vm.logicalFlowUrl = null;
        vm.physicallFlowUrl = null;
        vm.physicalRows = vm.physicalFlowsByDirection.ALL;
    }

    vm.filterPhysicalFlows = (direction) => {
        vm.physicalRows = vm.physicalFlowsByDirection[direction];
    }

    function refreshFlows() {
        vm.logicalRows = filterFlowOnDataTypes(vm.allLogicalFlows, vm.filteredDataTypes);
    }

    vm.onSelectDataType = (dt) => {
        vm.filteredDataTypes = _.concat(vm.filteredDataTypes, dt);
        refreshFlows();
    }

    vm.onDeselectDataType = (dt) => {
        vm.filteredDataTypes = _.without(vm.filteredDataTypes, dt);
        refreshFlows();
    }

    vm.clearAllDataTypes = () => {
        vm.filteredDataTypes = [];
        refreshFlows();
    }

    vm.addAllDataTypes = () => {
        vm.filteredDataTypes = _.map(vm.mappedDataTypes, d => d.dataTypeId);
        refreshFlows();
    }

    vm.hasRatings = (ratingByDefnId) => {
        return !_.isEmpty(ratingByDefnId);
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