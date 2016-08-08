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

const BINDINGS = {
    name: '@',
    icon: '@',
    small: '@'
};


function controller($anchorScroll,
                    $document,
                    $location,
                    $scope,
                    $window) {
    const vm = this;

    vm.sticky = false;

    vm.$onChanges = () => {
        $document[0].title = `Waltz: ${vm.name}`;
    };

    angular
        .element($window)
        .bind("scroll", () => {
            vm.sticky = $window.pageYOffset > 60;
            $scope.$apply();
        });


    vm.goTo = (sectionId) => {
        $location.hash(sectionId);
        $anchorScroll();
    };
}


controller.$inject=[
    '$anchorScroll',
    '$document',
    '$location',
    '$scope',
    '$window'
];


export default () => ({
    restrict: 'E',
    replace: false,
    template: require('./page-header.html'),
    scope: {},
    bindToController: BINDINGS,
    controller,
    controllerAs: 'ctrl',
    transclude: true
});

