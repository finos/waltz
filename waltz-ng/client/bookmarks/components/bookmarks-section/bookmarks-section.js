/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import _ from "lodash";
import {CORE_API} from '../../../common/services/core-api-utils';
import {initialiseData,isEmpty} from "../../../common";

import template from './bookmarks-section.html';


const bindings = {
    parentEntityRef: '<',
    showFilter: '@?'
};


const initialState = {
    filteredBookmarks: [],
    bookmarkKind: null,
    showFilter: false
};


function filterBookmarks(bookmarks = [], kind = null) {
    if (isEmpty(bookmarks)) return [];

    const byKind = _.groupBy(bookmarks, 'kind');

    const groupsToShow = kind
        ? { kind : byKind[kind] }
        : byKind;

    return _.map(groupsToShow, (v, k) => ({ kind: k, bookmarks: v }));
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        if(vm.parentEntityRef) {
            serviceBroker
                .loadViewData(CORE_API.BookmarkStore.findByParent, [vm.parentEntityRef])
                .then(r => vm.bookmarks = r.data)
                .then(() => vm.filteredBookmarks = filterBookmarks(vm.bookmarks, vm.bookmarkKind));
        }
    };

    vm.selectBookmarkKind = (kind) => {
        vm.bookmarkKind = kind;
        vm.filteredBookmarks = filterBookmarks(vm.bookmarks, vm.bookmarkKind);
    };
}


controller.$inject = ['ServiceBroker'];


const component = {
    bindings,
    template,
    controller
};


export default {
    component,
    id: 'waltzBookmarksSection'
};