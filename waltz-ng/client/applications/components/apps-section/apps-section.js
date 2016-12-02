import _ from "lodash";
import {initialiseData} from "../../../common";


const bindings = {
    apps: '<',
    endUserApps: '<',
    sourceDataRatings: '<',
};


const initialState = {
    apps: [],
    endUserApps: [],
    sourceDataRatings: [],
};


const template = require('./apps-section.html');


function combine(apps = [], endUserApps = []) {
    return _.concat(apps, endUserApps);
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        vm.combinedApps = combine(vm.apps, vm.endUserApps);
    };

    vm.onInitialise = (cfg) => {
        vm.export = () => cfg.exportFn(`apps.csv`);
    };

    vm.dismissSourceDataOverlay = () => {
        vm.visibility.appOverlay = false;
    };
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default component;