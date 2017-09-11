import template from './tree-picker.html';
import {initialiseData} from "../../../common/index";
import {buildHierarchies, doSearch, prepareSearchNodes, switchToParentIds} from "../../../common/hierarchy-utils";
import _ from "lodash";

const bindings = {
    onSelect: '<',
    items: '<',
    placeholder: '@?'
};

const initialState = {
    placeholder: 'Search...'
};


function prepareTree(items = []) {
    const hierarchy = switchToParentIds(buildHierarchies(items));
    return hierarchy;
}


function prepareExpandedNodes(hierarchy = []) {
    return hierarchy.length < 6  // pre-expand small trees
        ? _.clone(hierarchy)
        : [];
}


function controller() {

    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        const items = _.map(vm.items, d => {
            const concrete =  _.isUndefined(d.concrete) ? true : d.concrete;
            return Object.assign({}, d, { concrete })
        });
        vm.searchNodes = prepareSearchNodes(items);
        vm.hierarchy = prepareTree(items);
    };

    vm.treeOptions = {
        nodeChildren: "children",
        dirSelectable: true,
        equality: (a, b) => a && b && a.id === b.id
    };

    vm.doSearch = (termStr = '') => {
        const matchingNodes = doSearch(termStr, vm.searchNodes);
        vm.hierarchy = prepareTree(matchingNodes);
        vm.expandedNodes = prepareExpandedNodes(vm.hierarchy);
    };
}


controller.$inject = [];


export const component = {
    controller,
    bindings,
    template
};


export const id = 'waltzTreePicker';
