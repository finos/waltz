import {initialiseData, isDescendant, invokeFunction} from "../common";

const bindings = {
    name: '@',
    icon: '@',
    small: '@',
    visible: '<',
    onDismiss: '<'
};


const initialState = {
    onDismiss: () => console.log('no default onDismiss handler for overlay-panel')
}


const template = require('./overlay-panel.html');


function controller($element,
                    $document,
                    $scope,
                    $timeout) {

    const vm = initialiseData(this, initialState);

    const onClick = (e) => {
        const element = $element[0];
        if(!isDescendant(element, e.target)) {
            $scope.$apply(() => {
                invokeFunction(vm.onDismiss);
            })

        }
    };


    vm.$onChanges = (changes) => {
        if(changes.visible) {
            if(changes.visible.currentValue) {
                // set event handler - timeout is so that it doesn't appear and get dismissed immediately
                $timeout(() => $document.on('click', onClick), 200);
            }
            else {
                // unset event handler
                $document.off('click', onClick);
            }
        }
    }
}


controller.$inject = [
    '$element',
    '$document',
    '$scope',
    '$timeout'
];


const component  = {
    bindings,
    template,
    controller,
    transclude: true
};


export default component;
