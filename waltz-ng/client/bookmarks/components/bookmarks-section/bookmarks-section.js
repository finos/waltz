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
import {initialiseData,isEmpty} from "../../../common";

const bindings = {
    bookmarks: '<',
    entityId: '@',
    kind: '@', // entity-kind - not bookmark kind !
    parentName: '@',
    sourceDataRatings: '<'
};


const initialState = {
    filteredBookmarks: [],
    bookmarkKind: null
};


const template = require('./bookmarks-section.html');


function filterBookmarks(bookmarks = [], kind = null) {
    if (isEmpty(bookmarks)) return [];

    const byKind = _.groupBy(bookmarks, 'kind');

    const groupsToShow = kind
        ? { kind : byKind[kind] }
        : byKind;

    return _.map(groupsToShow, (v, k) => ({ kind: k, bookmarks: v }));
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        if(vm.bookmarks) {
            vm.filteredBookmarks = filterBookmarks(vm.bookmarks, vm.bookmarkKind);
        }
    };

    vm.selectBookmarkKind = (kind) => {
        vm.bookmarkKind = kind;
        vm.filteredBookmarks = filterBookmarks(vm.bookmarks, vm.bookmarkKind);
    };
}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;