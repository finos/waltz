import _ from 'lodash';


const bindings = {
    logicalFlows: '<',
    physicalFlows: '<'
};


const template = require('./physical-data-article-consumers.html');


function controller() {
    const vm = this;

    vm.$onChanges = () => {
        vm.flowData = combineFlowData(vm.logicalFlows, vm.physicalFlows);
    };

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
