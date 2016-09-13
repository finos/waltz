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

const BINDINGS = {
    role: '@waltzHasRole'
};

function controller(UserService) {
    const vm = this;
    UserService
        .whoami()
        .then(user => vm.show = UserService.hasRole(user, vm.role));

    vm.show = false;
}

controller.$inject = ['UserService'];


export default () => ({
    replace: true,
    restrict: 'A',
    transclude: true,
    scope: {},
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    template: '<span ng-show="ctrl.show"><ng-transclude></ng-transclude></span>',
    controller
});
