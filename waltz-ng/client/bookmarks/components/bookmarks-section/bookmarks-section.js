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
import {initialiseData} from "../../../common";

const bindings = {
    bookmarks: '<',
    entityId: '@',
    kind: '@',
    parentName: '@',
    sourceDataRatings: '<'
};


const initialState = {
    bookmarksByKind: {},
    filteredBookmarks: {},
    kindFilter: null
}


const template = require('./bookmarks-section.html');


function filterBookmarks(bookmarks = {}, kind = null) {
    const filterBookmarksMap = (kind)? {kind : bookmarks[kind]} : bookmarks;
    return _.map(filterBookmarksMap, (v, k) => ({ kind: k, bookmarks: v }));
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        if(vm.bookmarks) {
            vm.bookmarksByKind = _.groupBy(vm.bookmarks, 'kind');
            vm.filteredBookmarks = filterBookmarks(vm.bookmarksByKind, vm.kindFilter);
        }
    };


    vm.selectBookmarkKind = (kind) => {
        const filteredBookmarks = filterBookmarks(vm.bookmarksByKind, kind);
        if(kind === null || filteredBookmarks[kind]) {
            vm.kindFilter = kind;
            vm.filteredBookmarks = filterBookmarks(vm.bookmarksByKind, vm.kindFilter);
        }

    };
}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;