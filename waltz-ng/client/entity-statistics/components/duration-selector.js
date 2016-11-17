import _ from 'lodash';
import {initialiseData} from '../../common';


const bindings = {
    duration: '<',
    onChange: '<'
};


const durations = [
    { code: 'WEEK', name: "1w" },
    { code: 'MONTH', name: "1m" },
    { code: 'QUARTER', name: "3m" },
    { code: 'HALF_YEAR', name: "6m" },
    { code: 'YEAR', name: "1y" }
];


function mkOptions(active) {
    return _.map(
        durations,
        d => Object.assign(
            {},
            d,
            { active: d.code === active }));
}


const initialState = {
    selection: 'MONTH',
    options: mkOptions('MONTH'),
    onChange: (d) => console.log('duration-selector: onChange: ', d)
};


const template = require('./duration-selector.html');


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => vm.options = mkOptions(vm.duration);
    vm.onClick = (d) => vm.onChange(d.code);
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default component;
