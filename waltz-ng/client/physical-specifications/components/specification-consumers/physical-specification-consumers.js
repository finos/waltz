import {initialiseData} from '../../../common';

const bindings = {
    physicalFlows: '<',
    onFlowSelect: '<'
};


const template = require('./physical-specification-consumers.html');


const initialState = {
    physicalFlows: [],
    onFlowSelect: (flow) => console.log('wpsc: on-flow-select', flow)
};


function controller() {
    initialiseData(this, initialState);
}


const component = {
    bindings,
    controller,
    template
};


export default component;
