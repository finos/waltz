import {initialiseData} from '../../../common';


const bindings = {
    app: '<',
    appCapabilities: '<',
    capabilities: '<',
    ratings: '<',
    sourceDataRatings: '<'
};


const template = require('./app-function-tabgroup-section.html');


const initialState = {
    activeTab: 0,
    app: null,
    appCapabilities: [],
    capabilities: [],
    ratings: null,
    visibility: {
        overlay: false
    }
};


function controller() {
    const vm = initialiseData(this, initialState);
    vm.$onChanges = () =>
        console.log(vm.appCapabilities.length, vm.capabilities.length);
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};

export default component;