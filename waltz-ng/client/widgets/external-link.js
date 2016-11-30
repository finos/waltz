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
import {toDomain} from "../common";

const BINDINGS = {
    url: '@',
    title: '@',
    showUrl: '='
};


function toPrettyUrl(url = "") {
    return _.truncate( toDomain(url), { length: 60 });
}


function controller($scope) {

    const vm = this;

    $scope.$watchGroup(
        ['ctrl.url', 'ctrl.title', 'ctrl.showUrl'],
        ([url, title, showUrl = false]) => {
            vm.prettyTitle = title
                ? title
                : toPrettyUrl(url);
            vm.prettyUrl = toPrettyUrl(url);
            vm.showAside = (showUrl && title && url);
        });

}


controller.$inject = [
    '$scope'
];


export default () => ({
    replace: true,
    restrict: 'E',
    scope: {},
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller,
    template: require('./external-link.html')
});

