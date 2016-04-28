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
import {bookmarkNames} from "../../common/services/display_names.js";
import {bookmarkIconNames} from "../../common/services/icon_names.js";


function createKinds() {
    return _.chain(_.keys(bookmarkNames))
        .union(_.keys(bookmarkIconNames))
        .sortBy()
        .map(k => ({
            code: k,
            name: bookmarkNames[k] || '?',
            icon: bookmarkIconNames[k] || 'square-o'
        }))
        .value();
}

function controller($scope) {

    const kinds = createKinds();

    $scope.$watch('ctrl.value', (nv) => {
        if (nv) {
            const selection = _.find(kinds, { code: nv });
            this.selected = selection;
        }
    });

    this.onChange = (kind) => {
        if (!this.onSelect()) {
            console.warn('bookmark-kind-select: No handler for change notification is registered.  Please provide an on-select callback')
            return;
        }
        this.onSelect()(kind.code, kind);
    }

    this.kinds = kinds;


}

controller.$inject = ['$scope'];

export default () => ({
    restrict: 'E',
    replace: true,
    scope: {
        value: '@',
        onSelect: '&'
    },
    template: require('./bookmark-kind-select.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true
});
