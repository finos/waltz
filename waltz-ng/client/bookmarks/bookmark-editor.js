
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

import angular from 'angular';

function getParentRef(params) {
    return {
        id: params.entityId,
        kind: params.kind
    };
}


function controller(appStore, bookmarkStore, params, $state) {
    const parentRef = getParentRef(params);


    this.save = (b) => {
        b.parent = parentRef;
        bookmarkStore
            .save(b)
            .then(() => {
                this.refresh(parentRef);
                this.resetForm();
            });
    };

    this.cancel = () => {
        $state.go('main.app-view', { id: parentRef.id });
    };

    this.refresh = (ref) => {
        bookmarkStore
            .findByParent(ref)
            .then(bs => this.bookmarks = bs);
    };

    this.resetForm = () => {
        this.createShowing = false;
        this.editShowing = false;
        this.bookmark = {kind: 'DOCUMENTATION'};
    };


    this.remove = (b) => {
        if (confirm('Are you sure you want to remove this bookmark ?')) {
            bookmarkStore
                .remove(b.id)
                .then(() => this.refresh(parentRef));
        }
    };

    this.showCreate = () => {
        this.createShowing = true;
        this.editShowing = false;
        this.newBookmark = { kind: 'DOCUMENTATION' };
    };

    this.edit = (b) => {
        this.createShowing = false;
        this.editShowing = true;
        this.selectedBookmark = angular.copy(b);
    };

    // ---

    appStore.getById(parentRef.id).then(a => this.parent = a);
    this.refresh(parentRef);

}


controller.$inject = ['ApplicationStore', 'BookmarkStore', '$stateParams', '$state'];

export default {
    template: require('./bookmark-editor.html'),
    controller,
    controllerAs: 'ctrl'
};

