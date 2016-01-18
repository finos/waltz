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

function controller(userService, $state) {
    const vm = this;

    const reloadPage = () => $state.reload();

    const getCurrentUser = () => userService
        .whoami(true) // force
        .then(user => vm.user = user);

    const rejected = () => alert('Invalid username/password');

    const successful = () => getCurrentUser()
        .then(reloadPage);

    const login = (credentials) => userService
        .login(credentials)
        .then(successful, rejected);

    const logout = () => userService
        .logout()
        .then(getCurrentUser)
        .then(reloadPage);

    vm.login = login;
    vm.logout = logout;

    // -- boot ---

    getCurrentUser();
}

controller.$inject = ['UserService', '$state'];

function directive() {
    return {
        restrict: 'E',
        replace: true,
        template: require('./login-panel.html'),
        scope: {},
        bindToController: true,
        controller,
        controllerAs: 'ctrl'
    };
}
export default directive;