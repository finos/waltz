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

import {initialiseData} from "../../common";
import template from './change-log-table.html';

const bindings = {
    entries: '<',
    onInitialise: '<'
};



const initialState = {
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.columnDefs = [
        {
            field: 'severity',
            name: 'Severity',
            width: '10%',
            cellFilter: "toDisplayName:'severity'"
        },
        {
            field: 'message',
            name: 'Message',
            width: '70%'
        },
        {
            field: 'userId',
            name: 'User',
            width: '10%',
            cellTemplate: '<div class="ui-grid-cell-contents"><a ui-sref="main.profile.view ({userId: COL_FIELD})"><span ng-bind="COL_FIELD"></span></a></div>'
        },
        {
            field: 'createdAt',
            name: 'Timestamp',
            width: '10%',
            cellTemplate: '<div class="ui-grid-cell-contents"><waltz-from-now timestamp="COL_FIELD"></waltz-from-now></div>'
        }
    ];

}


const component = {
    bindings,
    template,
    controller
};


export default component;
