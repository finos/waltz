import {CORE_API} from '../../../common/services/core-api-utils';
import {initialiseData} from '../../../common';

import template from './change-initiative-section.html';


const bindings = {
    name: '@',
    parentEntityRef: '<'
};


const initialState = {
    name: 'Change Initiatives',
    changeInitiatives: [],
    visibility: {
        sourcesOverlay: false
    }
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        if(vm.parentEntityRef) {
            let promise = null;
            if (vm.parentEntityRef.kind === 'PERSON') {
                promise = serviceBroker
                    .loadViewData(CORE_API.PersonStore.getById, [vm.parentEntityRef.id])
                    .then(person => serviceBroker.loadViewData(
                        CORE_API.InvolvementStore.findChangeInitiativesForEmployeeIdAndCIKind,
                        [person.data.employeeId, 'PROGRAMME']));
            } else if (vm.parentEntityRef.kind === 'CHANGE_INITIATIVE') {
                promise = serviceBroker.loadViewData(
                    CORE_API.ChangeInitiativeStore.findByParentId,
                    [vm.parentEntityRef.id]);
            } else {
                promise = serviceBroker.loadViewData(
                    CORE_API.ChangeInitiativeStore.findByRef,
                    [vm.parentEntityRef.kind, vm.parentEntityRef.id]);
            }

            promise
                .then(result => {
                    vm.changeInitiatives = result.data;
            });
        }
    };

    vm.onSelect = ci => vm.selectedChangeInitiative = ci;

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
    id: 'waltzChangeInitiativeSection'
};
