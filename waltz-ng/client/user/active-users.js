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

import _ from 'lodash';
import {initialiseData} from "../common";
import template from './active-users.html';


const initialState = {
    activeUsers: []
};


function controller($window,
                    accessLogStore) {

    const vm = initialiseData(this, initialState);

    vm.onDurationChange = (minutes) => {
        accessLogStore
                .findActiveUsers(minutes)
                .then(activeUsers => vm.activeUsers = activeUsers );
    };

    vm.emailUsers = () => {
        const users = _.map(vm.activeUsers, 'userId');
        $window.open('mailto:?bcc=' + users.join('; '));
    };
}


controller.$inject = [
    '$window',
    'AccessLogStore',
];


export default {
    template,
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};

