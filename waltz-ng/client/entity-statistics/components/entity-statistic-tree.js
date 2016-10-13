import {buildHierarchies, findNode, switchToParentIds} from "../../common";

const bindings = {
    definitions: '<',
    onSelection: '<',
    currentDefinitionId: '<'
};


const initialState = {
    expandedNodes: [],
    currentNode: null
};


const template = require('./entity-statistic-tree.html');


function buildDefinitionTree(definitions = []) {
    return switchToParentIds(buildHierarchies(definitions));
}


function findParents(forest, currentNode) {
    let parents = [];
    let ptr = currentNode;
    while (ptr && ptr.parent) {
        ptr = findNode(forest, ptr.parent);
        parents.push(ptr);
    }
    return parents;
}


function controller() {
    const vm = Object.assign(this, initialState);

    vm.$onChanges = () => {
        vm.definitionTree = buildDefinitionTree(vm.definitions);
        vm.currentNode = findNode(vm.definitionTree, vm.currentDefinitionId);
        vm.expandedNodes = findParents(vm.definitionTree, vm.currentNode);
    };

    vm.treeOptions = {
        nodeChildren: "children",
        dirSelectable: true,
        equality: (a, b) => a && b && a.id === b.id
    };

    vm.handleSelection = (node) =>  {
        if (node.id !== vm.currentDefinitionId) {
            vm.currentDefinitionId = node.id;
            vm.onSelection(node);
        }
    }
}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;