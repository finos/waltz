/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
import {combineFlowData, enrichConsumes} from "../../utilities";
import {initialiseData, termSearch} from "../../../common";
import {mkEntityLinkGridCell, mkLinkGridCell} from "../../../common/link-utils";


const bindings = {
    physicalFlows: '<',
    specifications: '<',
    onInitialise: '<',
    onChange: '<'
};


const initialState = {
    physicalFlows: [],
    specifications: [],
    onInitialise: (e) => {},
    onChange: (e) => {}
};


const template = require('./physical-data-section.html');


function mkData(specifications = { produces: [], consumes: [] },
                physicalFlows = [])
{

    const ownedData = combineFlowData(
        specifications.produces,
        physicalFlows);

    const produces = _.filter(ownedData, p => p.physicalFlow != null);

    const consumes = enrichConsumes(
        specifications.consumes,
        physicalFlows);

    const unusedSpecifications = _.chain(ownedData)
        .filter(p => !p.physicalFlow)
        .map('specification')
        .value();

    return { produces, consumes, unusedSpecifications };
}


function controller() {

    const vm = initialiseData(this, initialState);

    vm.produceColumnDefs = [
        Object.assign(mkLinkGridCell('Name', 'specification.name', 'physicalFlow.id', 'main.physical-flow.view'), { width: "20%"} ),
        { field: 'specification.externalId', displayName: 'Ext. Id', width: "8%" },
        Object.assign(mkEntityLinkGridCell('Receiver(s)', 'targetRef', 'left'), { width: "15%" }),
        { field: 'specification.format', displayName: 'Format', width: "8%", cellFilter: 'toDisplayName:"dataFormatKind"' },
        { field: 'physicalFlow.transport', displayName: 'Transport', width: "10%", cellFilter: 'toDisplayName:"transportKind"' },
        { field: 'physicalFlow.frequency', displayName: 'Frequency', width: "9%", cellFilter: 'toDisplayName:"frequencyKind"' },
        { field: 'specification.description', displayName: 'Description', width: "30%" }
    ];

    vm.consumeColumnDefs = [
        Object.assign(mkEntityLinkGridCell('Source Application', 'sourceRef', 'none'), { width: "15%"} ),
        Object.assign(mkLinkGridCell('Name', 'specification.name', 'physicalFlow.id', 'main.physical-flow.view'), { width: "20%"} ),
        { field: 'specification.externalId', displayName: 'Ext. Id', width: "10%" },
        { field: 'specification.format', displayName: 'Format', width: "8%", cellFilter: 'toDisplayName:"dataFormatKind"' },
        { field: 'physicalFlow.transport', displayName: 'Transport', width: "14%", cellFilter: 'toDisplayName:"transportKind"' },
        { field: 'physicalFlow.frequency', displayName: 'Frequency', width: "10%", cellFilter: 'toDisplayName:"frequencyKind"' },
        { field: 'specification.description', displayName: 'Description', width: "23%" }
    ];

    vm.unusedSpecificationsColumnDefs = [
        { field: 'name', displayName: 'Name' },
        { field: 'format', displayName: 'Format', cellFilter: 'toDisplayName:"dataFormatKind"' },
        { field: 'description', displayName: 'Description' }
    ];

    const produceFields = _.map(vm.produceColumnDefs, 'field');

    const consumeFields = _.map(vm.consumeColumnDefs, 'field');

    vm.$onChanges = () => {
        Object.assign(vm, mkData(vm.specifications, vm.physicalFlows));
        vm.filterProduces("");
        vm.filterConsumes("");
    };

    function notifyChange() {
        // callback
        vm.onChange({
            producesCount: _.size(vm.filteredProduces),
            consumesCount: _.size(vm.filteredConsumes),
            unusedSpecificationsCount: _.size(vm.unusedSpecifications)
        });
    }

    vm.filterProduces = (query) => {
        vm.filteredProduces = termSearch(vm.produces, query, produceFields);
        notifyChange();
    };

    vm.filterConsumes = (query) => {
        vm.filteredConsumes = termSearch(vm.consumes, query, consumeFields);
        notifyChange();
    };

    vm.onProducesGridInitialise = (e) => {
        vm.producesExportFn = e.exportFn;
    };

    vm.onConsumesGridInitialise = (e) => {
        vm.consumesExportFn = e.exportFn;
    };

    vm.onUnusedSpecificationsGridInitialise = (e) => {
        vm.unusedSpecificationsExportFn = e.exportFn;
    };

    vm.exportProduces = () => {
        vm.producesExportFn('produces.csv');
    };

    vm.exportConsumes = () => {
        vm.consumesExportFn('consumes.csv');
    };

    vm.exportUnusedSpecifications = () => {
        vm.unusedSpecificationsExportFn('unused-specifications.csv');
    };

    // callback
    vm.onInitialise({
        exportProducesFn: vm.exportProduces,
        exportConsumesFn: vm.exportConsumes,
        exportUnusedSpecificationsFn: vm.exportUnusedSpecifications
    });
}

controller.$inject = ['$scope'];


const component = {
    template,
    bindings,
    controller
};


export default component;