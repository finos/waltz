import _ from "lodash";
import {kindToViewState} from "../../../common";

const bindings = {
    parents: '<',
    children: '<'
};


const initialState = {
    parents: [],
    children: []
};


const template = require('./immediate-hierarchy-navigator.html');


function enrichWithLink(node, $state) {
    const state = kindToViewState(node.entityReference.kind);
    const params = {
        id: node.entityReference.id
    };
    const url = $state.href(state, params);

    return {
        name: node.entityReference.name,
        url
    };
}


function prepareLinks(nodes = [], $state) {
    return _.map(nodes, n => enrichWithLink(n, $state));
}


function controller($state) {

    const vm = _.defaultsDeep(this, initialState);

    vm.$onChanges = ((changes) => {
        vm.childLinks = prepareLinks(vm.children, $state);
        vm.parentLinks = prepareLinks(vm.parents, $state);
    });
}

controller.$inject = ['$state'];

const component = {
    template,
    controller,
    bindings
};


export default component;