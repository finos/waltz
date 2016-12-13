
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

function controller(accessLogStore,
                    changeLogStore,
                    $state) {

    const vm = this;

    vm.accesslogs = [];
    vm.changeLogs = [];

    vm.onChange = (userName) => {
        if (userName === null || userName === '') {
            vm.accessLogs = [];
            vm.changeLogs = [];
        } else { accessLogStore
                .findForUserName(userName)
                .then(logs => vm.accesslogs = logs);

            changeLogStore
                .findForUserName(userName)
                .then(logs => vm.changeLogs = logs);
        }

    };


    vm.visit = (log) => {
        try {
            $state.go(log.state, JSON.parse(log.params));
        } catch (e) {
            console.error('failed to get to state', log, e);
        }
    };
}



controller.$inject = [
    'AccessLogStore',
    'ChangeLogStore',
    '$state'
];


// ---
export default {
    template: require('./user-log.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};

