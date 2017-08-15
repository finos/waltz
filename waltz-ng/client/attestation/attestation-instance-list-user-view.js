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


function controller(serviceBroker,
                    userService) {
    const vm = initialiseData(this, initialState);

    userService
        .whoami()
        .then(user => vm.user = user);

    const sampleRun = [{
        id: 1,
        targetEntityKind: 'APPLICATION',
        name: 'test run',
        description: 'test',
        selectorEntityKind: 'ORG_UNIT',
        selectorEntityId: 4326,
        selectorHierarchyScope: 'CHILDREN',
        involvementKindIds: 'ITAO',
        issuedBy: 'david.watkins@db.com',
        issuedOn: '2017-08-14',
        dueDate: '2017-08-24',
    }];

    const loadData = () => {
        serviceBroker
            .loadViewData(CORE_API.AttestationInstanceStore.findByUser, [], { force: true })
            .then(r => vm.attestations = mkAttestationData(sampleRun, r.data));
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
    'ServiceBroker',
    'UserService'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
}