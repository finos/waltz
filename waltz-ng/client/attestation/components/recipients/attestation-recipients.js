import _ from 'lodash';
import {CORE_API} from "../../../common/services/core-api-utils";
import {initialiseData} from '../../../common';
import template from './attestation-recipients.html';


const bindings = {
    instance: '<'
};

const initialState = {
    recipientReferences: [],
    visibility: {
        loading: false
    }
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

        if(vm.instance){
            vm.recipientReferences = [];
            vm.visibility.loading = true;
            serviceBroker
                .loadViewData(CORE_API.AttestationInstanceStore.findPersonsById, [vm.instance.id])
                .then(r => {
                    vm.recipientReferences = _.map(r.data, mkEntityRef) ;
                    vm.visibility.loading = false;
                });
        }
    };
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
    id: 'waltzAttestationRecipients'
};
