
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

import {nest} from "d3-collection";
import _ from "lodash";
import {aggregatePeopleInvolvements} from "../involvement/involvement-utils";


const CHANGE_LOG_LIMIT = 20;


function addDataTypes(extras, vm) {
    const existing = vm.dataTypes ? vm.dataTypes : [];
    vm.dataTypes = _.union(existing, extras);
    return vm.dataTypes;
}


function addOrgUnits(extras, vm) {
    const existing = vm.orgUnits ? vm.orgUnits : [];
    vm.orgUnits = _.union(existing, extras);
    vm.orgUnitsById = _.keyBy(vm.orgUnits, 'id');
    return vm.orgUnits;
}


export function loadDataFlows(dataFlowStore, id, vm) {
    return dataFlowStore
        .findByEntityReference('APPLICATION', id)
        .then(flows => vm.flows = flows);
}


export function loadPhysicalFlows(physicalFlowStore, entityReference, vm) {
    return physicalFlowStore
        .findByEntityReference(entityReference)
        .then(xs => vm.physicalFlows = xs);
}


export function loadChangeLog(changeLogStore, ref, vm) {
    changeLogStore
        .findByEntityReference(ref, CHANGE_LOG_LIMIT)
        .then(log => vm.log = log);
}


export function loadSourceDataRatings(sourceDataRatingStore, vm) {
    sourceDataRatingStore
        .findAll()
        .then(sdrs => vm.sourceDataRatings = sdrs);
}


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


export function loadInvolvements($q, involvementStore, id, vm) {
    $q.all([
        involvementStore.findByEntityReference('APPLICATION', id),
        involvementStore.findPeopleByEntityReference('APPLICATION', id)
    ]).then(([involvements, people]) => {
        vm.peopleInvolvements = aggregatePeopleInvolvements(involvements, people);
    });
}


export function loadAuthSources(authSourceStore, orgUnitStore, appId, ouId, vm) {
    const appAuthSourcePromise = authSourceStore.findByApp(appId);

    appAuthSourcePromise
        .then(authSources => nest()
            .key(a => a.dataType)
            .key(a => a.rating)
            .object(authSources))
        .then(nested => vm.authSources = nested)
        .then(nested => addDataTypes(_.keys(nested), vm));

    const loadDeclaringOUs = (authSources) => {
        const orgUnitIds = _
            .chain(authSources)
            .filter(a => a.parentReference.kind === 'ORG_UNIT')
            .map(a => a.parentReference.id)
            .uniq()
            .value();

        orgUnitStore
            .findByIds(orgUnitIds)
            .then(orgUnits => addOrgUnits(orgUnits, vm))
    };

    return appAuthSourcePromise
        .then(loadDeclaringOUs);
}


export function loadDataTypeUsages(dataTypeUsageStore, appId, vm) {
    return dataTypeUsageStore
        .findForEntity('APPLICATION', appId)
        .then(usages => vm.dataTypeUsages = usages);
}


export function loadLogicalFlowDecorators(store, appId, vm) {
    const selector = {
        entityReference: { id: appId, kind: 'APPLICATION'},
        scope: 'EXACT'
    };

    return store
        .findBySelectorAndKind(selector, 'DATA_TYPE')
        .then(r => vm.dataFlowDecorators = r);
}
