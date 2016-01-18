
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

import _ from 'lodash';

function controller(userStore) {

    const vm = this;

    userStore.findAll().then(users => vm.users = users);

    vm.dismiss = () => {
        vm.newUser = null;
        vm.selectedUser = null;
        vm.newPassword1 = null;
        vm.newPassword2 = null;
    };

    vm.userSelected = (user) => {
        vm.dismiss();
        vm.selectedUser = user;
        vm.roleSelections = _.foldl(
            user.roles,
            (acc, role) => { acc[role] = true; return acc; },
            {});
    };

    vm.addUserSelected = () => {
        vm.dismiss();
        vm.newUser = { userName: '', password: ''};
    };

    vm.isValidNewUser = (user) => {
        return user.userName && user.password;
    };

    vm.registerUser = (user) => {
        userStore.register(user)
            .then(
                r => {
                    vm.userSelected(user);
                    vm.users = [...vm.users, user];
                },
                err => {
                    console.error('Error registering user: ', err)
                    vm.lastError = err.data;
                }
            );
    };

    vm.updateUser = (user, roleSelections, password1, password2) => {

        if (password1 !== password2) {
            vm.lastError = { id: 'MISMATCH', message: 'Passwords do not match'}
            return;
        }


        const roles = _.chain(roleSelections)
            .map((v, k) => v ? k : null)  // get selected key name or null if not selected
            .compact() // remove non selected names
            .value();

        userStore
            .updateRoles(user.userName, roles)
            .then(
                () => {
                    user.roles = roles;
                    vm.dismiss();
                },
                e => {
                    vm.lastError = e.data;
                }
            );

        if (password1) {
            userStore.resetPassword(user.userName, password1);
        }
    };

    vm.hasRole = (user, role) => {
        const existingRoles = user.roles || [];
        return existingRoles.indexOf(role) > -1;
    };

    vm.deleteUser = (user) => {
        userStore
            .deleteUser(user.userName)
            .then(
                () => {
                    vm.users = _.reject(vm.users, u => u === user);
                    vm.dismiss();
                },
                e => vm.lastError = e.data
            );

        return false; // prevent form submission
    };
}



controller.$inject = [ 'UserStore' ];


// ---
export default {
    template: require('./user-management.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};

