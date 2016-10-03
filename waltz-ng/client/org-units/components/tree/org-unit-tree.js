
/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */
import _ from "lodash";


const bindings = {
    tree: '<',
    onSelection: '<'
};


/**
 * Pre-expand the tree a little bit
 * @param tree
 * @returns {Array}
 */
function expandTree(tree = []) {
    if (! tree || tree.length === 0) return;
    const expandedNodes =  _.concat(tree, tree[0].children);
    return expandedNodes;
}


const treeOptions =  {
    nodeChildren: "children",
    dirSelectable: true,
    equality: (a, b) => a && b && a.id === b.id
};


function controller() {
    const vm = this;

    vm.expandedNodes = [];

    vm.treeOptions = treeOptions;

    vm.onNodeSelect = (node) => {
        if (node.children && node.children.length > 0) {
            const idx = _.findIndex(vm.expandedNodes, n => n.id === node.id);
            if (idx === -1) {
                vm.expandedNodes.push(node);
            } else {
                vm.expandedNodes.splice(idx, 1);
            }
        }
        if (_.isFunction(vm.onSelection)) vm.onSelection(node);
    };

    vm.hasOwnApps = (node) => node.appCount && node.appCount > 0;
    vm.hasAnyApps = (node) => node.totalAppCount && node.totalAppCount > 0;
    vm.hasInheritedApps = (node) => node.childAppCount && node.childAppCount > 0;

    vm.hasOwnEndUserApps = (node) => node.endUserAppCount && node.endUserAppCount > 0;
    vm.hasAnyEndUserApps = (node) => node.totalEndUserAppCount && node.totalEndUserAppCount > 0;
    vm.hasInheritedEndUserApps = (node) => node.childEndUserAppCount && node.childEndUserAppCount > 0;

    vm.$onChanges = (changes) => {
        if (changes.tree) {
            _.each(
                expandTree(vm.tree),
                n => vm.expandedNodes.push(n));
        }
    };

}

controller.$inject = [ ];


const template = require('./org-unit-tree.html');


const component = {
    bindings,
    template,
    controller
};


export default component;

