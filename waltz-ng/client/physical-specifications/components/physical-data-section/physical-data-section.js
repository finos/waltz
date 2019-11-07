/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";
import {initialiseData} from "../../../common";
import {mkEntityLinkGridCell, mkEnumGridCell} from "../../../common/grid-utils";
import template from "./physical-data-section.html";
import {isRemoved} from "../../../common/entity-utils";


const bindings = {
    primaryRef: "<",
    physicalFlows: "<",
    specifications: "<",
    logicalFlows: "<",
    onInitialise: "<"
};


const initialState = {
    header: "All Flows",
    primaryRef: null,  // entityReference
    physicalFlows: [],
    logicalFlows: [],
    specifications: [],
    selectedFilter: "ALL",
    onInitialise: (e) => {}
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

const _allFlows = (entityRef) => (f) => true;

function mkData(primaryRef,
                specifications = { produces: [], consumes: [] },
                physicalFlows = [],
                logicalFlows = [],
                filter = _allFlows)
{
    if (!primaryRef) return [];

    const specsById = _.keyBy(specifications, "id");
    const logicalById = _.keyBy(logicalFlows, "id");

    const enrichFlow = (pf) => {
        return {
            physicalFlow: Object.assign({}, pf, {name: _.get(specsById[pf.specificationId], 'name')}),
            logicalFlow: logicalById[pf.logicalFlowId],
            specification: specsById[pf.specificationId]
        };
    };

    const filteredFlows = _
        .chain(physicalFlows)
        .reject(isRemoved)
        .map(enrichFlow)
        .reject(isIncomplete)
        .filter(filter(primaryRef))
        .value();

    return { filteredFlows };
}


function controller() {

    const vm = initialiseData(this, initialState);

    vm.columnDefs = [
        Object.assign(mkEntityLinkGridCell("Name", "physicalFlow", "left"), { width: "25%"} ),
        { field: "specification.externalId", displayName: "Ext. Id", width: "5%" },
        Object.assign(mkEntityLinkGridCell("Source", "logicalFlow.source", "left"), { width: "15%"} ),
        Object.assign(mkEntityLinkGridCell("Target", "logicalFlow.target", "left"), { width: "15%" }),
        Object.assign(mkEnumGridCell("Observation", "physicalFlow.freshnessIndicator", "FreshnessIndicator", true, true), { width: "10%"}),
        { field: "physicalFlow.criticality", displayName: "Criticality", width: "5%", cellFilter: "toDisplayName:\"physicalFlowCriticality\"" },
        { field: "specification.description", displayName: "Description", width: "25%" },
        // hidden columns - for local filtering
        { field: "specification.format", displayName: "Format", visible:false, cellFilter: "toDisplayName:\"dataFormatKind\"" },
        { field: "physicalFlow.transport", displayName: "Transport", visible:false, cellFilter: "toDisplayName:\"TransportKind\"" },
        { field: "physicalFlow.frequency", displayName: "Frequency", visible:false, cellFilter: "toDisplayName:\"frequencyKind\"" }
    ];

    vm.unusedSpecificationsColumnDefs = [
        { field: "name", displayName: "Name" },
        { field: "format", displayName: "Format", cellFilter: "toDisplayName:\"dataFormatKind\"" },
        { field: "description", displayName: "Description" }
    ];

    vm.$onChanges = () => {
        Object.assign(vm, mkData(vm.primaryRef, vm.specifications, vm.physicalFlows, vm.logicalFlows, _allFlows));
    };

    vm.changeFilter = (str) => {
        switch (str) {
            case "ALL":
                Object.assign(vm, mkData(vm.primaryRef, vm.specifications, vm.physicalFlows, vm.logicalFlows, _allFlows));
                vm.header = "All Flows";
                break;
            case "PRODUCES":
                Object.assign(vm, mkData(vm.primaryRef, vm.specifications, vm.physicalFlows, vm.logicalFlows, _isProducer));
                vm.header = "Produces";
                break;
            case "CONSUMES":
                Object.assign(vm, mkData(vm.primaryRef, vm.specifications, vm.physicalFlows, vm.logicalFlows, _isConsumer));
                vm.header = "Consumes";
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