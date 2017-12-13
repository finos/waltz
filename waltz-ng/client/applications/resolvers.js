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

// -- APP --
import {CORE_API} from "../common/services/core-api-utils";


export function appResolver(serviceBroker, $stateParams) {
    return serviceBroker
        .loadViewData(CORE_API.ApplicationStore.getById, [ $stateParams.id ])
        .then(r => r.data);
}

appResolver.$inject = ['ServiceBroker', '$stateParams'];


export function appByAssetCodeResolver(serviceBroker, $stateParams) {
    return serviceBroker
        .loadViewData(CORE_API.ApplicationStore.findByAssetCode, [ $stateParams.assetCode ])
        .then(r => r.data);
}

appByAssetCodeResolver.$inject = ['ServiceBroker', '$stateParams'];


// -- OUs --
export function orgUnitsResolver(serviceBroker) {
    return serviceBroker
        .loadAppData(CORE_API.OrgUnitStore.findAll)
        .then(r => r.data);
}

orgUnitsResolver.$inject = [ 'ServiceBroker' ];


