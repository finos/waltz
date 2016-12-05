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
import {initialiseData} from "../common";


const initialState = {
    activeUsers: []
};


function controller($window,
                    accessLogStore) {

    const vm =  initialiseData(this, initialState);

    vm.onDurationChange = (minutes) => {
        accessLogStore
                .findActiveUsers(minutes)
                .then(activeUsers => vm.activeUsers = activeUsers );
    };

    vm.emailUsers = () => {
        const users = _.map(vm.activeUsers, 'userId');
        $window.open('mailto:?bcc=' + users.join(', '));
    };

    vm.columnDefs = [
        {
            field: 'userId',
            name: 'User',
            width: '30%',
            cellTemplate: '<div class="ui-grid-cell-contents"><a ui-sref="main.profile.view ({userId: COL_FIELD})"><span ng-bind="COL_FIELD"></span></a></div>'
        },
        {
            field: 'createdAt',
            name: 'Timestamp',
            width: '30%',
            cellTemplate: '<div class="ui-grid-cell-contents"><waltz-from-now timestamp="COL_FIELD"></waltz-from-now></div>'
        }
    ];
}


controller.$inject = [
    '$window',
    'AccessLogStore',
];


// ---
export default {
    template: require('./active-users.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};

