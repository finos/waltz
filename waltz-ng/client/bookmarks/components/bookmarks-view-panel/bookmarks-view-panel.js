/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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