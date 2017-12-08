import {initialiseData, invokeFunction} from '../../../common/index';

import template from './attestation-confirmation.html';


const bindings = {
    instance: '<',
    onConfirm: '<',
    onCancel: '<'
};


const initialState = {
    onConfirm: (attestation) => console.log('default onConfirm handler for attestation-confirmation: '+ instance),
    onCancel: () => console.log('default onCancel handler for attestation-confirmation')
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.confirm = (attestation) => {
        invokeFunction(vm.onConfirm, attestation);
    };

    vm.cancel = () => {
        invokeFunction(vm.onCancel);
    };
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller,
    transclude: true
};


export default {
    component,
    id: 'waltzAttestationConfirmation'
};
