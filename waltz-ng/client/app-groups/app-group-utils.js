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



import {CORE_API} from "../common/services/core-api-utils";
import _ from 'lodash';


/**
 * Determines if the current user is owner for the
 * given group reference.
 *
 * @param serviceBroker
 * @param entityRef
 */
export function isGroupOwner(serviceBroker, entityRef) {
    return determineGroupRoles(serviceBroker, entityRef)
        .then(roles => _.includes(roles, 'OWNER'));
}


export function determineGroupRoles(serviceBroker, entityRef) {
    let user = null;
    let members = [];

    const userPromise = serviceBroker
        .loadAppData(CORE_API.UserStore.whoami)
        .then(r => user = r.data);

    const groupPromise = serviceBroker
        .loadViewData(
            CORE_API.AppGroupStore.getById,
            [ entityRef.id ])
        .then(r => members = _.get(r, "data.members", []));

    return userPromise
        .then(() => groupPromise)
        .then(() => _
                .chain(members)
                .filter(m => m.userId === user.userName)
                .map('role')
                .value());
}