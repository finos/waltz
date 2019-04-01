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

import {initialiseData} from "../../common/index";
import template from "./playpen3.html";
import {CORE_API} from "../../common/services/core-api-utils";


const initialState = {
    parentEntityRef: {
        id: 100,
        kind: "ORG_UNIT"
    },
};

function controller($stateParams, serviceBroker) {
    const vm = initialiseData(this, initialState);

    serviceBroker
        .loadViewData(
            CORE_API.AllocationSchemeStore.findAll)
        .then( r => console.log({ schemes: r.data }));
    serviceBroker
        .loadViewData(
            CORE_API.AllocationStore.findByEntityAndScheme,
            [ {id: 27, kind: "APPLICATION"}, 1])
        .then( r => console.log({ allocs: r.data}));
}


controller.$inject = [
    "$stateParams",
    "ServiceBroker"
];


const view = {
    template,
    controller,
    controllerAs: "ctrl",
    bindToController: true,
    scope: {}
};


export default view;
