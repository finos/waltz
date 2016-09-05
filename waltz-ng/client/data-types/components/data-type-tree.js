import _ from "lodash";
import {initialiseData} from "../../common";


const bindings = {
    trees: '<',
    onSelection: '<'
};


const initialState = {
    expandedNodes: [],
};


const template = require('./data-type-tree.html');


function controller() {
    const vm = initialiseData(this, initialState);

    vm.treeOptions = {
        nodeChildren: "children",
        dirSelectable: true,
        equality: (a, b) => a && b && a.id === b.id
    };

    vm.hasOwnDataFlows = (node) => node.dataFlowCount && node.dataFlowCount > 0;
    vm.hasAnyDataFlows = (node) => node.totalDataFlowCount && node.totalDataFlowCount > 0;
    vm.hasInheritedDataFlows = (node) => node.childDataFlowCount && node.childDataFlowCount > 0;
    vm.onNodeSelect = (node) => {
        if (node.children && node.children.length > 0) {
            const idx = _.findIndex(vm.expandedNodes, n => n.id === node.id);
            if (idx === -1) {
                vm.expandedNodes.push(node);
            } else {
                vm.expandedNodes.splice(idx, 1);
            }
        }
        if (vm.onSelection &&  _.isFunction(vm.onSelection)) vm.onSelection(node);
    };

};


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;