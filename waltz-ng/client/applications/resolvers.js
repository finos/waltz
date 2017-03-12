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

// -- APP --
export function appResolver(appStore, $stateParams) {
    return appStore.getById($stateParams.id);
}

appResolver.$inject = ['ApplicationStore', '$stateParams'];


export function appByAssetCodeResolver(appStore, $stateParams) {
    return appStore.findByAssetCode($stateParams.assetCode);
}

appByAssetCodeResolver.$inject = ['ApplicationStore', '$stateParams'];



// -- OUs --
export function orgUnitsResolver(orgUnitStore) {
    return orgUnitStore.findAll();
}

orgUnitsResolver.$inject = [ 'OrgUnitStore' ];


