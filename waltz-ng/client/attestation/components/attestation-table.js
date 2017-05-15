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

import {initialiseData} from "../../common";
import {mkEntityLinkGridCell} from "../../common/link-utils";

const bindings = {
    entries: '<',
    onInitialise: '<'
};

const template = require('./attestation-table.html');


const initialState = {
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.columnDefs = [
        {
            field: 'attestationType',
            name: 'Type',
            width: '10%',
            cellFilter: "toDisplayName:'attestationType'"
        },
        {
            field: 'comments',
            name: 'Comments',
            width: '50%'
        },
        {
            field: 'attestedBy',
            name: 'User',
            width: '10%',
            cellTemplate: '<div class="ui-grid-cell-contents"><a ui-sref="main.profile.view ({userId: COL_FIELD})"><span ng-bind="COL_FIELD"></span></a></div>'
        },
        {
            field: 'attestedAt',
            name: 'Timestamp',
            width: '10%',
            cellTemplate: '<div class="ui-grid-cell-contents"><waltz-from-now timestamp="COL_FIELD"></waltz-from-now></div>'
        },
        Object.assign(mkEntityLinkGridCell('Attesting Entity', 'attestingEntityReference'), { width: "20%" })
    ];

}


const component = {
    bindings,
    template,
    controller
};


export default component;
