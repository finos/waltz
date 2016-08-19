import {initialiseData} from "../../common";


const bindings = {
    ratings: '<',
    flowData: '<',
    applications: '<',
};


const template = require('./data-type-flows-tabgroup-section.html');


const initialState = {
    ratings: [],
    flowData: null,
    applications: [],
    visibility: {
        flowConfigOverlay: false,
        flowConfigButton: false,
        sourcesOverlay: false
    }
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.tabChanged = (name, index) => {
        vm.visibility.flowConfigButton = index > 0;
        if(index === 0) vm.visibility.flowConfigOverlay = false;
    }

}


controller.$inject = [
];


const component = {
    bindings,
    controller,
    template
};


export default component;