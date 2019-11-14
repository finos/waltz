import template from "./physical-flow-section.html";


const bindings = {
    parentEntityRef: '<',
    optionalColumnDefs: '<'
};


function controller() {
    const vm = this;
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzPhysicalFlowSection'
};
