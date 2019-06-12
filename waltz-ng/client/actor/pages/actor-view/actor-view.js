/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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

import {initialiseData} from "../../../common";

import template from "./actor-view.html";
import {CORE_API} from "../../../common/services/core-api-utils";


const initialState = {
    logs: [],
};


function mkHistoryObj(actor) {
    return {
        name: actor.name,
        kind: 'ACTOR',
        state: 'main.actor.view',
        stateParams: { id: actor.id }
    };
}


function addToHistory(historyStore, actor) {
    if (! actor) { return; }

    const historyObj = mkHistoryObj(actor);

    historyStore.put(
        historyObj.name,
        historyObj.kind,
        historyObj.state,
        historyObj.stateParams);
}



function controller($stateParams,
                    dynamicSectionManager,
                    serviceBroker,
                    historyStore) {

    const vm = initialiseData(this, initialState);

    const id = $stateParams.id;
    vm.entityRef = { kind: 'ACTOR', id };

    vm.$onInit = () => {

        dynamicSectionManager.initialise("ACTOR");

        serviceBroker
            .loadViewData(
                CORE_API.ActorStore.getById,
                [ id ])
            .then(r => {
                vm.actor = r.data;
                vm.entityRef = Object.assign({}, vm.entityRef, { name: vm.actor.name });
                addToHistory(historyStore, vm.actor);
            });
    };


}


controller.$inject = [
    '$stateParams',
    'DynamicSectionManager',
    'ServiceBroker',
    'HistoryStore',
];


const view = {
    template,
    controller,
    controllerAs: 'ctrl'
};

export default view;
