
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

function controller(accessLogStore,
                    changeLogStore) {

    const vm = this;

    vm.accesslogs = [];
    vm.changeLogs = [];

    vm.onChange = (userName) => {
        if (userName === null || userName === '') {
            vm.accessLogs = [];
            vm.changeLogs = [];
        } else {
            accessLogStore
                .findForUserName(userName, 500)
                .then(logs => vm.accesslogs = logs);

            changeLogStore
                .findForUserName(userName, 500)
                .then(logs => vm.changeLogs = logs);
        }

    };

    vm.changeLogColumnDefs = [
        {
            field: 'severity',
            name: 'Severity',
            width: '10%',
            cellFilter: "toDisplayName:'severity'"
        },
        {
            field: 'message',
            name: 'Message',
            width: '50%'
        },
        {
            field: 'parentReference',
            name: 'Entity',
            width: '30%',
            cellTemplate: '<div class="ui-grid-cell-contents"><span ng-bind="COL_FIELD.kind"></span> / <span ng-bind="COL_FIELD.id"></span> <span ng-if="COL_FIELD.name"> <span ng-bind="COL_FIELD.name"></span></span></div>'
        },
        {
            field: 'createdAt',
            name: 'Timestamp',
            width: '10%',
            cellTemplate: '<div class="ui-grid-cell-contents"><waltz-from-now timestamp="COL_FIELD"></waltz-from-now></div>'
        }
    ];

    vm.accessLogColumnDefs = [
        {
            field: 'state',
            name: 'State',
            width: '20%',
        },
        {
            field: 'params',
            name: 'Params',
            width: '50%'
        },
        {
            field: 'createdAt',
            name: 'Timestamp',
            width: '20%',
            cellTemplate: '<div class="ui-grid-cell-contents"><waltz-from-now timestamp="COL_FIELD"></waltz-from-now></div>'
        },
        {
            field: 'params',
            name: '',
            width: '10%',
            cellTemplate: '<div class="ui-grid-cell-contents"><waltz-state-link state="row.entity.state" params="row.entity.params" link-text="Visit"></waltz-state-link></div>'
        },

    ];
}



controller.$inject = [
    'AccessLogStore',
    'ChangeLogStore',
];


// ---
export default {
    template: require('./user-log.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};

