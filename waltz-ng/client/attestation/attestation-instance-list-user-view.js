import _ from 'lodash';
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

        $q.all([runsPromise, instancesPromise])
            .then(([runs, instances]) => {
                const instancesByRunId = _.groupBy(instances, 'attestationRunId');

                vm.runsWithInstances =  _.chain(runs)
                    .map(r => Object.assign({}, r, { instances: instancesByRunId[r.id]}))
                    .filter(r => r.instances)
                    .value();
            });
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