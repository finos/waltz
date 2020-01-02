/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */
import _ from 'lodash';
import template from './bookmarks-view-panel.html';
import {initialiseData} from "../../../common/index";


const bindings = {
    bookmarks: '<',
    parentEntityRef: '<'
};


const initialState = {
    qry: null,
    bookmarkKind: null,
    filteredBookmarks: []
};


function filterBookmarks(bookmarks = [], kind = null, qry = null) {
    if (_.isEmpty(bookmarks)) return [];

    const qryStr = _.toLower(qry);

    const byKind = _
        .chain(bookmarks)
        .filter(b => _.isEmpty(qryStr)
            ? true
            : b.searchStr.indexOf(qryStr) > -1)
        .groupBy(b => b.bookmarkKind)
        .value();

    const groupsToShow = kind
        ? { kind : byKind[kind] }
        : byKind;

    return _.map(groupsToShow, (v, k) => ({ kind: k, bookmarks: v }));
}


function enrichBookmarkWithSearchStr(bookmark) {
    const searchStr = _.toLower(`${bookmark.description} ${bookmark.title} ${bookmark.url}`);
    return Object.assign({}, bookmark, { searchStr });
}


function controller() {
    const vm = initialiseData(this, initialState);

    const refresh = () => {
        vm.filteredBookmarks = filterBookmarks(vm.enrichedBookmarks, vm.bookmarkKind, vm.qry);
    };

    vm.$onChanges = () => {
        if (vm.bookmarks) {
            vm.enrichedBookmarks = _.map(
                vm.bookmarks,
                enrichBookmarkWithSearchStr);
        }
        refresh();
    };

    vm.selectBookmarkKind = (kind) => {
        vm.bookmarkKind = kind;
        refresh();
    };

    vm.onQueryStrChange = (qry) => {
        vm.qry = qry;
        refresh();
    };

}


const component = {
    controller,
    template,
    bindings
};


export default {
    id: 'waltzBookmarksViewPanel',
    component
};