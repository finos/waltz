import {initialiseData, invokeFunction} from "../../../common";

const bindings = {
    selected: '<',
    onSelect: '<'
};


const template = require('./rating-picker.html');


const initialState = {
    onSelect: (rating) => 'No onSelect handler defined for rating-picker: ' + rating,
    options: [
        { value: 'G', label: 'Good', clazz: 'rating-G' },
        { value: 'A', label: 'Adequate', clazz: 'rating-A' },
        { value: 'R', label: 'Poor', clazz: 'rating-R' },
        { value: 'Z', label: 'Unknown', clazz: 'rating-Z' }
    ]
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