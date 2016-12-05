
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
import {isEmpty} from "../../common";


function controller($scope) {

    this.confirmLabel = this.confirmLabel || 'Save';

    this.onKindSelect = (code) => {
        this.bookmark.kind = code;
    };

    this.togglePrimary = () => {
        this.bookmark.isPrimary = !this.bookmark.isPrimary;
    };


    this.submit = () => {
        if (!this.onSubmit || !this.onSubmit()) {
            console.warn('bookmark-form: submission handler missing.  Please provide an on-submit callback');
            return;
        }
        this.onSubmit()(this.bookmark);
    };

    this.cancel = () => {
        if (!this.onCancel) {
            console.warn('bookmark-form: cancel handler missing.  Please provide an on-cancel callback');
            return;
        }
        this.onCancel();
    };

    const recalcFormStatus = () => {
        const { url } = this.bookmark;
        this.submitDisabled = isEmpty(url);
    };

    $scope.$watch('ctrl.bookmark', recalcFormStatus, true);
    recalcFormStatus();
}

controller.$inject = ['$scope'];

export default () => ({
    restrict: 'E',
    replace: true,
    scope: {
        bookmark: '=',
        onSubmit: '&',
        onCancel: '&',
        confirmLabel: '@'
    },
    template: require('./bookmark-form.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true
});
