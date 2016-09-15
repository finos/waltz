import _ from "lodash";
import {initialiseData} from "../../common";


const bindings = {
    trees: '<',
    onHighlight: '<',
    onSelect: '<',
    selectedNodes: '<'
};


const initialState = {
    expandedNodes: [],
    selectedNodes: [],
    onSelect: node => console.log('default handler in multi-select-treecontrol for node selected: ', node)
};


const template = require('./multi-select-treecontrol.html');


function controller() {
    const vm = initialiseData(this, initialState);

    vm.treeOptions = {
        nodeChildren: "children",
        dirSelectable: true,
        equality: (a, b) => a && b && a.id === b.id,
        multiSelection: true
    };

    vm.onNodeHighlight = (node) => {
        if (node.children && node.children.length > 0) {
            const idx = _.findIndex(vm.expandedNodes, n => n.id === node.id);
            if (idx === -1) {
                vm.expandedNodes.push(node);
            } else {
                vm.expandedNodes.splice(idx, 1);
            }
        }
        if (vm.onHighlight &&  _.isFunction(vm.onHighlight)) vm.onHighlight(node);
    };

    vm.onNodeSelect = (node) => {
        if(node.checked) {
            vm.selectedNodes.push(node);
        } else{
            _.remove(vm.selectedNodes, node);
        }
        if (vm.onSelect &&  _.isFunction(vm.onSelect)) vm.onSelect(node);

        event.stopPropagation();
    }

};


controller.$inject = [];


const component = {
    bindings,
    template,
    controller,
    transclude: true
};


export default component;