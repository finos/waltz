import {initialiseData} from '../../../../common';

import {buildHierarchies, switchToParentIds} from "../../../../common/hierarchy-utils";

import template from './db-change-initiative-browser.html';


const bindings = {
    changeInitiatives: '<',
    scrollHeight: '<',
    onSelect: '<'
};


const initialState = {
    containerClass: [],
    changeInitiatives: [],
    treeData: [],
    visibility: {
        sourcesOverlay: false
    },
    onSelect: (d) => console.log('wcib: default on-select', d),
};


function prepareTreeData(data = []) {
    return switchToParentIds(buildHierarchies(data));
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (c) => {
        if(vm.changeInitiatives) {
            vm.treeData = prepareTreeData(vm.changeInitiatives);
        }

        if (vm.scrollHeight && vm.treeData && vm.treeData.length > 10) {
            vm.containerClass = [
                 `waltz-scroll-region-${vm.scrollHeight}`
            ];
        }
    }
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzDbChangeInitiativeBrowser'
};
