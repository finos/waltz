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
import {nest} from "d3-collection";
import {CORE_API} from "../common/services/core-api-utils";
import {initialiseData} from "../common/index";

import template from "./attestation-instance-list-user-view.html";
import {attest} from "./attestation-utils";
import {displayError} from "../common/error-utils";
import ToastStore from "../notification/components/toaster/toast-store"
import {entity} from "../common/services/enums/entity";


const initialState = {
    runsWithInstances: [],
    selectedAttestation: null,
    showAttested: false,
    attestNext: false
};


function controller($q,
                    serviceBroker,
                    userService,
                    notification) {
    const vm = initialiseData(this, initialState);

    userService
        .whoami()
        .then(user => vm.user = user);

    const loadData = () => {
        const runsPromise = serviceBroker
            .loadViewData(CORE_API.AttestationRunStore.findByRecipient)
            .then(r => r.data);

        const instancesPromise = serviceBroker
            .loadViewData(CORE_API.AttestationInstanceStore.findByUser, [vm.showAttested], {force: true})
            .then(r => r.data);

        const historicalInstancesPromise = serviceBroker
            .loadViewData(CORE_API.AttestationInstanceStore.findHistoricalForPendingByUser, [], {force: true})
            .then(r => r.data);

        serviceBroker.loadAppData(CORE_API.NotificationStore.findAll, [], { force: true });

        return $q.all([runsPromise, instancesPromise, historicalInstancesPromise])
            .then(([runs, instances, historicInstances]) => {
                const historicByParentRefByChildKind = nest()
                    .key(d => d.parentEntity.kind)
                    .key(d => d.parentEntity.id)
                    .key(d => d.childEntityKind)
                    .object(historicInstances);

                const instancesWithHistoricByRunId = _.chain(instances)
                    .map(i => Object.assign(
                        {},
                        i,
                        { historic: _.get(historicByParentRefByChildKind, [i.parentEntity.kind, i.parentEntity.id, i.childEntityKind], []) } ))
                    .groupBy("attestationRunId")
                    .value();

                vm.runsWithInstances =  _.chain(runs)
                    .map(r => Object.assign({}, r, { instances: instancesWithHistoricByRunId[r.id] }))
                    .filter(r => r.instances)
                    .sortBy(r => r.dueDate)
                    .value();
            });
    };

    loadData();

    // interaction
    vm.onAttestEntity = () => {
        const instance = vm.selectedAttestation;

        attest(serviceBroker, instance.parentEntity, instance.attestedEntityKind)
            .then(() => loadData())
            .then(() => {
                const currentRun = _.find(vm.runsWithInstances, r => r.id === instance.attestationRunId);
                const remainingInstances = currentRun.instances;

                return vm.selectedAttestation = vm.attestNext && !_.isEmpty(remainingInstances)
                    ? _.head(remainingInstances)
                    : null
            })
            .then(() => ToastStore.success(`Attested ${_.get(entity, [instance.attestedEntityKind, "name"], "unknown subject")} for ${instance.parentEntity.name} successfully!`))
            .catch(e => displayError(notification, "Could not attest", e));
    };

    vm.onToggle = () => {
        vm.attestNext = !vm.attestNext;
    }

    vm.onCancelAttestation = () => {
        vm.selectedAttestation = null;
        vm.attestNext = false;
    };

    vm.onToggleFilter = () => {
        vm.showAttested = !vm.showAttested;
        loadData();
    };

}


controller.$inject = [
    "$q",
    "ServiceBroker",
    "UserService",
    "Notification"
];


export default {
    template,
    controller,
    controllerAs: "ctrl"
}