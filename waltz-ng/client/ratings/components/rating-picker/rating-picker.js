import {initialiseData, invokeFunction} from "../../../common";

const bindings = {
    selected: '<',
    onSelect: '<'
};


const template = require('./rating-picker.html');


const initialState = {
    onSelect: (rating) => 'No onSelect handler defined for rating-picker: ' + rating
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.select = (rating) => {
        invokeFunction(vm.onSelect, rating);
    };

}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;