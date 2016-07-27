
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

import angular from "angular";
import {kindToViewState} from "../common";


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
        vm.newBookmark = { kind: 'DOCUMENTATION' };
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
    template: require('./bookmark-editor.html'),
    controller,
    controllerAs: 'ctrl'
};

