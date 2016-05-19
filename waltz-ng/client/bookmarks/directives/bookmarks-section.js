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

const BINDINGS = {
    bookmarks: '=',
    entityId: '@',
    kind: '@',
    parentName: '@',
    sourceDataRatings: '='
};

function controller($scope) {
    const vm = this;

    $scope.$watch(
        'ctrl.bookmarks',
        (bookmarks => {
            if (! bookmarks) return;
            vm.bookmarksByKind = _.groupBy(bookmarks, 'kind');
        }));

}

controller.$inject = ['$scope'];

export default () => ({
    restrict: 'E',
    replace: true,
    scope: {},
    template: require('./bookmarks-section.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: BINDINGS
});
