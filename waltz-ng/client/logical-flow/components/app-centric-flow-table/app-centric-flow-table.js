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
import template from './app-centric-flow-table.html';

const bindings = {
    app: '<',
    flows: '<',
    decorators: '<',
    onSelect: '<'
};


const initialState = {
    onSelect: (app) => console.log("Default handler for appCentricFlowTable.onSelect(). ", app)
};




function enrichAndGroupFlows(app, flows = [], decorators = []) {
    if(!app) return {};

    const dataTypeDecoratorsByFlowId = _
        .chain(decorators)
        .filter(d => d.decoratorEntity.kind === "DATA_TYPE")
        .map(d => ({
            dataFlowId: d.dataFlowId,
            id: d.decoratorEntity.id,
            rating: d.rating
        }))
        .keyBy('dataFlowId')
        .value();

    const groupedFlows = _
        .chain(flows)
        .filter(f => f.target.id === app.id || f.source.id === app.id)
        .map(f => Object.assign({}, f, {direction: f.target.id === app.id ? 'Incoming' : 'Outgoing'}))
        .map(f => Object.assign({}, f, {decorator: dataTypeDecoratorsByFlowId[f.id]}))
        .map(f => Object.assign({}, f, {counterpart: f.direction === 'Incoming' ? f.source : f.target}))
        .sortBy('direction')
        .groupBy('direction')
        .value();

    return groupedFlows;
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = changes => {
        vm.groupedFlows = enrichAndGroupFlows(vm.app, vm.flows, vm.decorators);
    };
}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;