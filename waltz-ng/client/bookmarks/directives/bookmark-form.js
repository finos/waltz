
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
