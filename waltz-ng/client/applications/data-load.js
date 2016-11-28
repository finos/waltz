
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


export function loadChangeLog(changeLogStore, id, vm) {
    changeLogStore
        .findByEntityReference('APPLICATION', id, CHANGE_LOG_LIMIT)
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
    const ouAuthSourcesPromise = authSourceStore.findByReference('ORG_UNIT', ouId);

    ouAuthSourcesPromise.then(ouas => vm.ouAuthSources = ouas);
    appAuthSourcePromise.then(aas => vm.appAuthSources = aas);

    appAuthSourcePromise.then(authSources => nest()
        .key(a => a.dataType)
        .key(a => a.rating)
        .map(authSources))
        .then(nested => vm.authSources = nested)
        .then(nested => addDataTypes(_.keys(nested), vm));

    return appAuthSourcePromise
        .then(authSources => _.chain(authSources)
            .filter(a => a.parentReference.kind === 'ORG_UNIT')
            .map(a => a.parentReference.id)
            .uniq()
            .value())
        .then(orgUnitIds => orgUnitStore.findByIds(orgUnitIds))
        .then(orgUnits => addOrgUnits(orgUnits, vm));
}


export function loadDataTypeUsages(dataTypeUsageStore, appId, vm) {
    return dataTypeUsageStore
        .findForEntity('APPLICATION', appId)
        .then(usages => vm.dataTypeUsages = usages);
}


export function loadDataFlowDecorators(store, appId, vm) {
    const selector = {
        entityReference: { id: appId, kind: 'APPLICATION'},
        scope: 'EXACT'
    };

    return store
        .findBySelectorAndKind(selector, 'DATA_TYPE')
        .then(r => vm.dataFlowDecorators = r);
}
