import {initialiseData, buildHierarchies} from "../../common";
import {buildPropertySummer} from "../../common/tally-utils";


const bindings = {
    dataTypes: '<',
    tallies: '<'
};


const initialState = {
    dataTypeHierarchy: []
};


const template = require('./data-type-tree.html');


function prepareDataTypeTree(dataTypes, tallies) {

    const dataTypesByCode = _.keyBy(dataTypes, 'code');

    const enrichWithDirectCounts = (tallies, keyName) => {
        _.each(tallies, t => {
            const dt = dataTypesByCode[t.id];
            if (dt) dt[keyName] = t.count;
        });
    };

    enrichWithDirectCounts(tallies, "dataFlowCount");

    const rootDataTypes = buildHierarchies(dataTypes);

    const dataFlowCountSummer = buildPropertySummer("dataFlowCount", "totalDataFlowCount", "childDataFlowCount");

    _.each(rootDataTypes, dataFlowCountSummer);

    return rootDataTypes;
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes => {
        if(vm.tallies) {
            vm.dataTypeHierarchy = prepareDataTypeTree(vm.dataTypes, vm.tallies);
        }
    });

    vm.hasOwnDataFlows = (node) => node.dataFlowCount && node.dataFlowCount > 0;
    vm.hasAnyDataFlows = (node) => node.totalDataFlowCount && node.totalDataFlowCount > 0;
    vm.hasInheritedDataFlows = (node) => node.childDataFlowCount && node.childDataFlowCount > 0;
};


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;