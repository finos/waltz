import _ from "lodash";
import {initialiseData, buildHierarchies} from "../../common";


const bindings = {
    items: '<',
    onClick: '<',
    onCheck: '<',
    onUncheck: '<',
    checkedItemIds: '<',
    expandedItemIds: '<'
};


const initialState = {
    items: [],
    expandedItemIds: [],
    expandedNodes: [],
    checkedItemIds: [],
    checkedMap: {},
    trees: [],
    onCheck: node => console.log('default handler in multi-select-treecontrol for node check: ', node),
    onUncheck: node => console.log('default handler in multi-select-treecontrol for node uncheck: ', node),
    onClick: node => console.log('default handler in multi-select-treecontrol for node click: ', node),
};


const template = require('./multi-select-treecontrol.html');


function invokeHandler(handler) {
    if (handler &&  _.isFunction(handler)) {
        const parameters = _.slice(arguments, 1);
        handler(...parameters);
    }
}


function buildTrees(nodes) {
    return buildHierarchies(nodes);
}


function mkCheckedMap(nodes = [], checked = []) {
    return _.reduce(nodes, (acc, n) => {
        acc[n.id] = _.includes(checked, n.id);
        return acc;
    }, {});
}


function mkExpandedNodes(nodes = [], expandedNodeIds = []) {
    function recurse(nodes, nodeIds) {
        const filteredNodes = _.filter(nodes, n => nodeIds.includes(n.id));
        const newParentIds = _.chain(filteredNodes)
            .filter(n => n.parentId)
            .map('parentId')
            .value();

        if(newParentIds.length > 0) {
            return [filteredNodes, recurse(nodes, newParentIds)];
        } else {
            return [filteredNodes];
        }
    }

    return _.flattenDeep(recurse(nodes, expandedNodeIds));
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.treeOptions = {
        nodeChildren: "children",
        dirSelectable: true,
        equality: (a, b) => a && b && a.id === b.id,
        multiSelection: true
    };

    vm.onNodeClick = (node) => {
        if (node.children && node.children.length > 0) {
            const idx = _.findIndex(vm.expandedNodes, n => n.id === node.id);
            if (idx === -1) {
                vm.expandedNodes.push(node);
            } else {
                vm.expandedNodes.splice(idx, 1);
            }
        }
        invokeHandler(vm.onClick, node.id);
    };

    vm.onNodeCheck = (node) => {
        invokeHandler(vm.onCheck, node.id);
        event.preventDefault();
        event.stopPropagation();
    };

    vm.onNodeUncheck = (node) => {
        invokeHandler(vm.onUncheck, node.id);
        event.preventDefault();
        event.stopPropagation();
    };

    vm.$onChanges = changes => {
        if(changes.items) {
            vm.trees = buildTrees(vm.items);
        }

        if(changes.items || changes.expandedItemIds) {
            if (vm.expandedItemIds && vm.items) {
                vm.expandedNodes = mkExpandedNodes(vm.items, vm.expandedItemIds);
            }
        }

        vm.checkedMap = mkCheckedMap(vm.items, vm.checkedItemIds);
    };

};


controller.$inject = [];


const component = {
    bindings,
    template,
    controller,
    transclude: true
};


export default component;