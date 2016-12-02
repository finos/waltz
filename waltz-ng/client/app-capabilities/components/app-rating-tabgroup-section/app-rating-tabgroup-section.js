import {initialiseData} from "../../../common";


const bindings = {
    app: '<',
    appCapabilities: '<',
    capabilities: '<',
    processes: '<',
    sourceDataRatings: '<'
};


const template = require('./app-rating-tabgroup-section.html');


const initialState = {
    activeTab: 0,
    app: null,
    appCapabilities: [],
    capabilities: [],
    processes: [],
    visibility: {
        overlay: false
    }
};


function controller() {
    const vm = initialiseData(this, initialState);


    vm.dismissSourceDataOverlay = () => {
        vm.visibility.overlay = false;
    }
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};

export default component;