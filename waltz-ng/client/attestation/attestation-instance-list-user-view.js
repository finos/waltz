import _ from 'lodash';
import {CORE_API} from "../common/services/core-api-utils";
import {initialiseData} from "../common/index";

import template from './attestation-instance-list-user-view.html';


const initialState = {
    attestationInstancesAndRuns: [],
    selectedAttestation: null
};


function mkAttestationData(attestationRuns = [], attestationInstances = []){
    const runsById = _.keyBy(attestationRuns, 'id');

    const mappedData = _.map(attestationInstances, instance => {
        return {
            'instance': instance,
            'run': runsById[instance.attestationRunId]
        }
    });

    const [incomplete = [], complete = []] = _.partition(mappedData,
        data => data.instance.attestedAt === null);

    return {
        'incomplete': incomplete,
        'complete': complete
    };
}


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
            .loadViewData(CORE_API.AttestationInstanceStore.findByUser, [], {force: true})
            .then(r => r.data);

        $q.all([runsPromise, instancesPromise])
            .then(([runs, instances]) => vm.attestations = mkAttestationData(runs, instances));
    };

    loadData();

    // interaction
    vm.attestEntity = (attestation) => {
        serviceBroker
            .execute(CORE_API.AttestationInstanceStore.attestInstance, [attestation.instance.id])
            .then(() => loadData())
            .then(() => vm.selectedAttestation = null);
    };

    vm.cancelAttestation = () => {
        vm.selectedAttestation = null;
    }
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