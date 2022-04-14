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
import template from "./physical-data-section.html";
import {isRemoved} from "../../../common/entity-utils";
import {columnDef, withWidth} from "../../../physical-flow/physical-flow-table-utilities";


const bindings = {
    primaryRef: "<",
    physicalFlows: "<",
    specifications: "<",
    logicalFlows: "<",
    onInitialise: "<?"
};


const initialState = {
    header: "All Flows",
    primaryRef: null,  // entityReference
    physicalFlows: [],
    logicalFlows: [],
    specifications: [],
    selectedFilter: "ALL",
};


/**
 * Given an enriched flow returns a boolean indicating
 * if any enriched fields are missing
 * @param f
 */
const isIncomplete = f => !f.logicalFlow ||  !f.specification;

/**
 * Takes a entityReference and returns a new function which takes an
 * enriched flow and returns true if the entity is producing the flow
 * or false if not
 * @param entityRef
 * @private
 */
const _isProducer = (entityRef) => (f) => {
    const source = f.logicalFlow.source;
    return source.id === entityRef.id && source.kind === entityRef.kind;
};

/**
 * Takes a entityReference and returns a new function which takes an
 * enriched flow and returns true if the entity is consuming the flow
 * or false if not
 * @param entityRef
 * @private
 */
const _isConsumer = (entityRef) => (f) => {
    const target = f.logicalFlow.target;
    return target.id === entityRef.id && target.kind === entityRef.kind;
};



function mkData(entityRef, specifications, logicalFlows, physicalFlows) {
    const specsById = _.keyBy(specifications, "id");
    const logicalById = _.keyBy(logicalFlows, "id");

    const enrichFlow = (pf) => {
        return {
            physicalFlow: Object.assign({}, pf, {name: _.get(specsById[pf.specificationId], "name")}),
            logicalFlow: logicalById[pf.logicalFlowId],
            specification: specsById[pf.specificationId]
        };
    };

    const enrichedFlows = _
        .chain(physicalFlows)
        .reject(isRemoved)
        .map(enrichFlow)
        .reject(isIncomplete)
        .value();

    const flowsByDirection = {
        ALL: [],
        UPSTREAM: [],
        DOWNSTREAM: []
    };

    const isProducer = _isProducer(entityRef);
    const isConsumer = _isConsumer(entityRef);

    enrichedFlows.forEach(f => {
        flowsByDirection.ALL.push(f);
        if (isProducer(f)) {
            flowsByDirection.UPSTREAM.push(f);
        }
        if (isConsumer(f)) {
            flowsByDirection.DOWNSTREAM.push(f);
        }
    });

    return flowsByDirection;
}


function controller() {

    const vm = initialiseData(this, initialState);

    vm.columnDefs = [
        withWidth(columnDef.name, "25%"),
        withWidth(columnDef.extId, "5%"),
        columnDef.source,
        columnDef.target,
        columnDef.criticality,
        columnDef.description
    ];

    vm.unusedSpecificationsColumnDefs = [
        { field: "name", displayName: "Name" },
        { field: "format", displayName: "Format", cellFilter: "toDisplayName:\"dataFormatKind\"" },
        { field: "description", displayName: "Description" }
    ];

    vm.$onChanges = () => {
        vm.flowsByDirection = mkData(
            vm.primaryRef,
            vm.specifications,
            vm.logicalFlows,
            vm.physicalFlows);
        vm.selectedFilter = "ALL";
        vm.gridData = vm.flowsByDirection.ALL;
    };

    vm.changeFilter = (str) => {
        switch (str) {
            case "ALL":
                vm.header = "All Flows";
                vm.gridData = vm.flowsByDirection.ALL;
                break;
            case "PRODUCES":
                vm.header = "Produces";
                vm.gridData = vm.flowsByDirection.UPSTREAM;
                break;
            case "CONSUMES":
                vm.header = "Consumes";
                vm.gridData = vm.flowsByDirection.DOWNSTREAM;
                break;
        }
    }
}

controller.$inject = ["$scope"];


const component = {
    template,
    bindings,
    controller
};


export default component;