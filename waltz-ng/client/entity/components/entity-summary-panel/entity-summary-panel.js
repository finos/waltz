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
import template from "./entity-summary-panel.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";


const bindings = {
    entityRef: "<"
};


const initialState = {

};


const loaders = {
    "APPLICATION": CORE_API.ApplicationStore.getById,
    "ACTOR": CORE_API.ActorStore.getById,
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    function loadEntity() {
        return serviceBroker
            .loadViewData(
                loaders[vm.entityRef.kind],
                [ vm.entityRef.id ])
            .then(r => vm.entity = r.data);
    }

    function load() {
        loadEntity()
            .then(entity => {
                if (entity.kind === "APPLICATION") {
                    serviceBroker
                        .loadViewData(
                            CORE_API.OrgUnitStore.getById,
                            [vm.entity.organisationalUnitId])
                        .then(r => vm.orgUnit = r.data);
                }
            });
    }

    vm.$onChanges = (c) => {
        if (vm.entityRef && c.entityRef) {
            load();
        }
    }
}

controller.$inject = ["ServiceBroker"];


const component = {
    bindings,
    template,
    controller
};


export default {
    id: "waltzEntitySummaryPanel",
    component
};