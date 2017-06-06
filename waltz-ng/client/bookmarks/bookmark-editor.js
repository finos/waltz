
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

import angular from "angular";
import {kindToViewState} from "../common/link-utils";

import template from './bookmark-editor.html';


function controller($state,
                    $stateParams,
                    $window,
                    bookmarkStore,
                    notification) {
    const vm = this;

    const parentRef = getParentRef($stateParams);

    vm.save = (b) => {
        b.parent = parentRef;
        bookmarkStore
            .save(b)
            .then(() => {
                vm.refresh(parentRef);
                vm.resetForm();
            })
            .then(() => notification.success('Updated bookmarks'))
    };

    vm.refresh = (ref) => {
        bookmarkStore
            .findByParent(ref)
            .then(bs => vm.bookmarks = bs);
    };

    vm.resetForm = () => {
        vm.createShowing = false;
        vm.editShowing = false;
        vm.bookmark = {kind: 'DOCUMENTATION'};
    };


    vm.remove = (b) => {
        if (confirm('Are you sure you want to remove this bookmark ?')) {
            bookmarkStore
                .remove(b.id)
                .then(() => vm.refresh(parentRef))
                .then(() => notification.warning('Removed bookmark'));
        }
    };

    vm.showCreate = () => {
        vm.createShowing = true;
        vm.editShowing = false;
        vm.newBookmark = {
            kind: 'DOCUMENTATION',
            lastUpdatedBy: "ignored, server will set"
        };
    };

    vm.edit = (b) => {
        vm.createShowing = false;
        vm.editShowing = true;
        vm.selectedBookmark = angular.copy(b);
    };

    vm.refresh(parentRef);

    vm.parentRef = parentRef;

    vm.goToParent = () => {
        try {
            const nextState = kindToViewState(parentRef.kind);
            if(nextState == 'main.entity-statistic.view') {
                // note: this is poor, but a necessary evil, fix as part of #2298
                // reason: main.entity-statistic.view take two id parameters and they get mixed up
                $window.history.back();
            }
            $state.go(nextState, parentRef);
        } catch (e) {
            $window.history.back();
        }
    };
}


function getParentRef(params) {
    return {
        id: params.entityId,
        kind: params.kind,
        name: params.parentName
    };
}


controller.$inject = [
    '$state',
    '$stateParams',
    '$window',
    'BookmarkStore',
    'Notification'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};

