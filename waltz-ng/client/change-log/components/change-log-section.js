import {initialiseData} from "../../common";


const bindings = {
    entries: '<',
    entityReference: '<'
};


const template = require('./change-log-section.html');


function controller() {
    const vm = initialiseData(this);

    vm.changeLogTableInitialised = (api) => {
        vm.exportChangeLog = api.export;
    };
}


const component = {
    bindings,
    template,
    controller
};


export default component;
