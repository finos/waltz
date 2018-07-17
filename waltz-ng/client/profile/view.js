/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import {dynamicSections} from "../dynamic-section/dynamic-section-definitions";
import template from './view.html';


const initialState = {
    changeLogSection: dynamicSections.changeLogSection,
    contribution: {
        score: 0,
        directScores: [],
        leaderBoard: []
    },
    directs: [],
    managers: [],
    person: null,
    roles: [],
    user: null,
    passwordResetEnabled: false
};


controller.$inject = [
    '$q',
    '$stateParams',
    'PersonStore',
    'SettingsService',
    'UserContributionStore',
    'UserStore',
    'UserService'
];


function controller($q,
                    $stateParams,
                    personStore,
                    settingsService,
                    userContributionStore,
                    userStore,
                    userService) {

    const vm = Object.assign(this, initialState);
    const userId = $stateParams.userId;

    const loadManagerAndDirects = (p) => {
        if (p) {
            const empId = p.employeeId;
            personStore
                .findManagers(empId)
                .then(managers => vm.managers = managers);

            personStore
                .findDirects(empId)
                .then(directs => vm.directs = directs);
        }
    };

    vm.userId = userId;

    const personPromise = personStore
        .findByUserId(userId)
        .then(p => vm.person = p);

    personPromise
        .then(loadManagerAndDirects);


    userStore
        .findForUserId(userId)
        .then(u => vm.user = u);

    userContributionStore
        .findForUserId(userId)
        .then(score => vm.contribution.score = score);

    userContributionStore
        .findForDirects(userId)
        .then(scores => vm.contribution.directScores = scores);

    userContributionStore
        .getLeaderBoard()
        .then(leaderBoard => vm.contribution.leaderBoard = leaderBoard);

    $q.all([userService.whoami(), settingsService.findOrDefault('web.authentication', null)])
        .then(([who, how]) => vm.passwordResetEnabled = who.userName === userId && how === 'waltz');

    vm.resetPassword = () => {
        if (vm.resetForm.password1 !== vm.resetForm.password2) {
            alert("Passwords do not match!");
            return;
        }

        if (vm.resetForm.password1.length < 4) {
            alert("Password too short (requires >= 4 chars)");
            return;
        }

        userStore
            .resetPassword(vm.userId, vm.resetForm.password1, vm.resetForm.currentPassword)
            .then(r => {
                if (!r) alert("Password reset failed");
                else alert("Password updated")
            });
    };

}


const view = {
    template,
    controller,
    controllerAs: 'ctrl'
};


export default view;

