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
import template from "./external-identifier-popover.html";
import {CORE_API} from "../../common/services/core-api-utils";
import {mkRef} from "../../common/entity-utils";
import {nest} from "d3-collection";
import {displayError} from "../../common/error-utils";


const bindings = {
    physicalFlow: "<",
    editable: "@"
};


const initialState = {
    editable: false
};


function controller(notification, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const load = () => {
        console.log("loading data", vm.physicalFlow);
        // debugger
        console.log("editable ", vm.editable);
        serviceBroker
            .loadViewData(
                CORE_API.ExternalIdentifierStore.findByEntityReference,
                [vm.entityRef],
                {force: vm.editable === "true"})
            .then(r => vm.externalIdentifiers = r.data);
    };

    vm.$onChanges = () => {
        if (vm.physicalFlow) {
            vm.entityRef = mkRef(vm.physicalFlow.kind, vm.physicalFlow.id);
            load();
        }
    };


    vm.removeExternalId = (externalId) => {
        if (confirm(`Are you sure you want to delete externalId ${externalId}?`)) {
            return serviceBroker
                .execute(
                    CORE_API.ExternalIdentifierStore.deleteById,
                    [vm.entityRef, encodeURIComponent(externalId)])
                .then(() => {
                    notification.success(`Deleted External Id ${externalId}`);
                    return load();
                })
                .catch(e => displayError(notification, "Could not delete value", e))
        }

    };
}

controller.$inject = [
    "Notification",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzExternalIdentifierPopover"
};
