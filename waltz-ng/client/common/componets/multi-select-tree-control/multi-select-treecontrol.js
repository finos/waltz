import _ from "lodash";
import {initialiseData, buildHierarchies, switchToParentIds, invokeFunction} from "../../../common";


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
    onCheck: id => console.log('default handler in multi-select-treecontrol for node id check: ', id),
    onUncheck: id => console.log('default handler in multi-select-treecontrol for node id uncheck: ', id),
    onClick: node => console.log('default handler in multi-select-treecontrol for node click: ', node),
};


const template = require('./multi-select-treecontrol.html');





function buildTrees(nodes) {
    return switchToParentIds(buildHierarchies(nodes));
}


function mkCheckedMap(nodes = [], checked = []) {
    return _.reduce(nodes, (acc, n) => {
        acc[n.id] = _.includes(checked, n.id);
        return acc;
    }, {});
}


function mkExpandedNodes(nodes = [], expandedIds = []) {
    function recurse(nodes, ids) {
        const filteredNodes = _.filter(nodes, n => ids.includes(n.id));
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

    return _.flattenDeep(recurse(nodes, expandedIds));
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.treeOptions = {
        nodeChildren: "children",
        dirSelectable: true,
        equality: (a, b) => a && b && a.id === b.id,
        multiSelection: false
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
        invokeFunction(vm.onClick, node.id);
    };

    vm.onNodeCheck = (id) => {
        invokeFunction(vm.onCheck, id);
        event.preventDefault();
        event.stopPropagation();
    };

    vm.onNodeUncheck = (id) => {
        invokeFunction(vm.onUncheck, id);
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