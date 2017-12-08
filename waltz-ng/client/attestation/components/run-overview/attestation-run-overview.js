import {CORE_API} from "../../../common/services/core-api-utils";
import {initialiseData} from '../../../common/index';

import template from './attestation-run-overview.html';


const bindings = {
    run: '<',
};


const initialState = {
    issuedByRef: null
};


function mkEntityRef(person) {
    if (person) {
        return {
            id: person.id,
            name: person.displayName,
            kind: 'PERSON'
        };
    }
    return person;
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        if(vm.run){
            serviceBroker
                .loadViewData(CORE_API.PersonStore.findByUserId, [vm.run.issuedBy])
                .then(r => {
                    vm.issuedByRef = mkEntityRef(r.data);
                });
        }
    }


    vm.getFriendlyScope = (scope) => {
        switch (scope) {
            case 'EXACT':
                return 'only';
            case 'PARENTS':
                return 'and it\'s parents';
            case 'CHILDREN':
                return 'and it\'s children';
            default:
                throw 'Unrecognised scope: ' + scope;
        }
    }
}


controller.$inject = [
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzAttestationRunOverview'
};
