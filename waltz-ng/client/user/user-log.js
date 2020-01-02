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
import {CORE_API} from "../common/services/core-api-utils";
// ---
import template from './user-log.html';


function controller(accessLogStore,
                    serviceBroker) {

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

            serviceBroker
                .loadViewData(CORE_API.ChangeLogStore.findForUserName, [vm.userName, 500])
                .then(result => vm.changeLogs = result.data);
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
            cellTemplate: '<div class="ui-grid-cell-contents"><waltz-grid-sref state="row.entity.state" params="row.entity.params" link-text="Visit"></waltz-grid-sref></div>'
        },

    ];
}



controller.$inject = [
    'AccessLogStore',
    'ServiceBroker',
];


export default {
    template,
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};

