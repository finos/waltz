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

import _ from "lodash";

const bindings = {
    logoOverlayText: '<'
};


const template = require('./navbar-profile.html');


const initialState = {
    logoOverlayText: '',
    user: null
};


function loginController($scope, $uibModalInstance, logoOverlayText) {
    $scope.ok = () => {
        const credentials = {
            userName: $scope.username,
            password: $scope.password
        };
        $uibModalInstance.close(credentials);
    };

    $scope.username = '';
    $scope.password = '';
    $scope.logoOverlayText = logoOverlayText || '';

    $scope.cancel = () => $uibModalInstance.dismiss('cancel');
}


loginController.$inject = [
    '$scope',
    '$uibModalInstance',
    'logoOverlayText'
];


function controller($state,
                    $uibModal,
                    settingsService,
                    userService) {
    const vm = _.defaultsDeep(this, initialState);

    settingsService
        .findOrDefault('web.authentication', "")
        .then(webAuthentication => {
            vm.allowDirectLogin = webAuthentication === 'waltz';
        });

    userService
        .whoami(true) // force
        .then(user => vm.user = user);



    const reloadPage = () => $state.reload();

    const rejected = () => alert('Invalid username/password');

    const logout = () => userService
        .logout()
        .then(reloadPage);


    vm.logout = logout;
    vm.login = () => {

        var loginModalInstance = $uibModal.open({
            animation: true,
            templateUrl: 'navbar/modal-login.html',
            controller: loginController,
            resolve: {
                logoOverlayText: () => vm.logoOverlayText
            },
            size: 'sm'
        });

        loginModalInstance.result
            .then(
                (credentials) => userService
                    .login(credentials)
                    .then(reloadPage, rejected),
                () => console.log('Login dismissed at: ' + new Date()));

    };

}


controller.$inject = [
    '$state',
    '$uibModal',
    'SettingsService',
    'UserService'];


const directive = {
    restrict: 'E',
    replace: true,
    scope: {},
    bindToController: bindings,
    controllerAs: 'ctrl',
    controller,
    template
};


export default () => directive;