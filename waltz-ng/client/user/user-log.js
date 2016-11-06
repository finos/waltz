
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

