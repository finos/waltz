
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

export function loadServers(serverInfoStore, appId, vm) {
    serverInfoStore
        .findByAppId(appId)
        .then(servers => vm.servers = servers);
}


export function loadSoftwareCatalog(catalogStore, appId, vm) {
    catalogStore.findByAppIds([appId])
        .then(resp => vm.softwareCatalog = resp);
}


export function loadDatabases(databaseStore, appId, vm) {
    databaseStore.findByAppId(appId)
        .then(resp => vm.databases = resp);
}


