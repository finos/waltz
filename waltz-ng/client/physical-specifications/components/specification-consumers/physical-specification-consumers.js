import _ from 'lodash';
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
    const vm = initialiseData(this, initialState);
}


function combineFlowData(logicalFlows = [], physicalFlows = []) {
    const logicalById = _.keyBy(logicalFlows, "id");
    return _.map(physicalFlows, physicalFlow => ({
        physicalFlow,
        logicalFlow: logicalById[physicalFlow.flowId]
    }));
}


const component = {
    bindings,
    controller,
    template
};


export default component;
