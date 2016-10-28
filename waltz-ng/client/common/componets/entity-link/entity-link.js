import {initialiseData, kindToViewState} from "../../../common";


const bindings = {
    entityRef: '<',
    iconPlacement: '<',
    tooltipPlacement: '<'
};


const template = require('./entity-link.html');


const initialState = {
    iconPlacement: 'left', // can be left, right, none
    tooltipPlacement: 'top' // left, top-left, top-right; refer to: (https://github.com/angular-ui/bootstrap/tree/master/src/tooltip)
};


function controller($state) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        if (vm.entityRef) {
            vm.viewState = kindToViewState(vm.entityRef.kind);
            if (vm.viewState) {
                // url needs to be re-computed when entityRef changes
                // eg: when used in a ui-grid cell template
                vm.viewUrl = $state.href(vm.viewState, { id: vm.entityRef.id });
            }
        }
    };
}

controller.$inject = [
    '$state'
];


const component = {
    bindings,
    template,
    controller
};


export default component;