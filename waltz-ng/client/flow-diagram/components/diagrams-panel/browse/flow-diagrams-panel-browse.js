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


import template from "./flow-diagrams-panel-browse.html";

import {initialiseData} from "../../../../common/index";
import {CORE_API} from "../../../../common/services/core-api-utils";


const bindings = {
    parentEntityRef: "<",
    canCreate: "<",
    onSelectDiagram: "<",
    onCreateDiagram: "<?",
    onEditDiagram: "<?",
};


const initialState = {};

function controller(serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        serviceBroker
            .loadViewData(
                CORE_API.FlowDiagramStore.findByEntityReference,
                [ vm.parentEntityRef ],
                { force: true })
            .then(r => {
                vm.diagrams = _.orderBy(r.data, "name");
            });
    }

}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    controller,
    template,
    bindings
};


export default {
    component,
    id: "waltzFlowDiagramsPanelBrowse"
};