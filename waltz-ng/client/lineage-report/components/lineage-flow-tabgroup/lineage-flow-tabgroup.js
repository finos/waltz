import {combineFlowData} from '../../../physical-specifications/utilities';

const template = require('./lineage-flow-tabgroup.html');


const bindings = {
    spec: '<',
    lineageData: '<'
};



function process(xs = []) {
    return {
        all: xs,
        byId: _.keyBy(xs, 'id')
    };
}


function controller() {
    const vm = this;

    vm.$onChanges = () => {
        if (! vm.lineageData) return;
        vm.specifications = process(vm.lineageData.specifications);
        vm.logicalFlows = process(vm.lineageData.logicalFlows);
        vm.physicalFlows = process(vm.lineageData.physicalFlows);
        vm.applications = process(vm.lineageData.applications);

        vm.flowData = combineFlowData(
            vm.specifications.all,
            vm.physicalFlows.all,
            vm.logicalFlows.all);
    }
}



const component = {
    controller,
    bindings,
    template
};


export default component;