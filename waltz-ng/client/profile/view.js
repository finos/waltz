/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import {dynamicSections} from "../dynamic-section/dynamic-section-definitions";
import template from './view.html';
import moment from "moment";


const initialState = {
    changeLogSection: dynamicSections.changeLogSection,
    contribution: {
        score: 0,
        directScores: [],
        leaderBoard: [],
        monthlyLeaderBoard: [],
        positionedLeaderBoard: []
    },
    directs: [],
    managers: [],
    person: null,
    roles: [],
    user: null,
    date: null,
    showMore: false,
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

    userContributionStore
        .getLeaderBoardLastMonth()
        .then(monthlyLeaderBoard => vm.contribution.monthlyLeaderBoard = monthlyLeaderBoard);

    userContributionStore
        .getRankedLeaderBoard(userId)
        .then(positionedLeaderBoard => vm.contribution.positionedLeaderBoard = positionedLeaderBoard);


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

    vm.date = moment().format('MMMM YYYY');

    vm.toggleRankings = () => {
        return vm.showMore = !vm.showMore;
    };

}


const view = {
    template,
    controller,
    controllerAs: 'ctrl'
};


export default view;

