/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
        if (changes.children) {
            vm.childLinks = prepareLinks(vm.children, $state);
        }
        if (changes.parents) {
            vm.parentLinks = prepareLinks(vm.parents, $state);
        }
    });
}

controller.$inject = ['$state'];

const component = {
    template,
    controller,
    bindings
};


export default component;