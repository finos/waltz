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

import _ from 'lodash';
import {nest} from "d3-collection";
import {CORE_API} from "../common/services/core-api-utils";
import {initialiseData} from "../common/index";

import template from './attestation-instance-list-user-view.html';


const initialState = {
    runsWithInstances: [],
    selectedAttestation: null,
    showAttested: false
};


function controller($q,
                    serviceBroker,
                    userService) {
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

        $q.all([runsPromise, instancesPromise, historicalInstancesPromise])
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
                    .groupBy('attestationRunId')
                    .value();

                vm.runsWithInstances =  _.chain(runs)
                    .map(r => Object.assign({}, r, { instances: instancesWithHistoricByRunId[r.id] }))
                    .filter(r => r.instances)
                    .sortBy(r => r.dueDate)
                    .value();
            });

        serviceBroker.loadAppData(CORE_API.NotificationStore.findAll, [], { force: true });
    };

    loadData();

    // interaction
    vm.attestEntity = (instance) => {
        serviceBroker
            .execute(CORE_API.AttestationInstanceStore.attestInstance, [instance.id])
            .then(() => loadData())
            .then(() => vm.selectedAttestation = null);
    };

    vm.cancelAttestation = () => {
        vm.selectedAttestation = null;
    };

    vm.toggleFilter = () => {
        vm.showAttested = !vm.showAttested;
        loadData();
    };

}


controller.$inject = [
    '$q',
    'ServiceBroker',
    'UserService'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
}