import { initialiseData, kindToViewState } from "../../../common";


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


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        if (vm.entityRef) {
            vm.viewState = kindToViewState(vm.entityRef.kind);
        }
    };
}


const component = {
    bindings,
    template,
    controller
};


export default component;