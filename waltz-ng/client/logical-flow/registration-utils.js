/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

export function onlySelectedTypes(dataTypeMap) {
    return _.chain(dataTypeMap)
        .reduce((acc, v, k) => {
            if (v) acc[k] = v;
            return acc;
        }, {})
        .keys()
        .value();
}

export function toRef(app) {
    return { id: app.id, kind: 'APPLICATION', name: app.name };
}

export function toCommandObject(model, originalTypes) {

    const source = toRef(model.source.app);
    const target = toRef(model.target.app);

    const updatedTypes = onlySelectedTypes(model.dataTypes);

    const addedTypes = _.difference(updatedTypes, originalTypes);
    const removedTypes = _.without(originalTypes, ...updatedTypes);

    return {
        source,
        target,
        addedTypes,
        removedTypes
    };
}


export function calculateDataTypeUsage(sourceApp, targetApp, flows) {

    if ( !sourceApp || !targetApp ) {
        return {};
    }

    const filterFn = f =>
    f.target.id === targetApp.id && f.source.id === sourceApp.id;


    const usageMap = _.chain(flows)
        .filter(filterFn)
        .map('dataType')
        .reduce((acc, t) => { acc[t] = true; return acc; }, {})
        .value();

    return usageMap;
}


export function loadAppAuthSources(authSourceStore, appId, vm) {
    return authSourceStore
        .findByApp(appId)
        .then(aas => vm.appAuthSources = aas);
}


export function loadOrgUnitAuthSources(authSourceStore, ouId, vm) {
    return authSourceStore
        .findByReference('ORG_UNIT', ouId)
        .then(ouas => vm.ouAuthSources = ouas);
}

export function loadDataTypes(dataTypeService, vm) {
    return dataTypeService
        .loadDataTypes()
        .then(xs => vm.dataTypes = xs);
}



