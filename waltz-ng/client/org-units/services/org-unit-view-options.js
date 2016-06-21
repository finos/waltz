/*
 *  This file is part of Waltz.
 *
 *  Waltz is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Waltz is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import _ from "lodash";


const options = {
    includeSubUnits: true,
    productionOnly: false,
    primaryOnly: false
};


function prepareAppCapabilities(apps, capabilities, rawAppCapabilities) {
    const appsById = _.keyBy(apps, 'id');
    const capabilitiesById = _.keyBy(capabilities, 'id');

    return _.chain(rawAppCapabilities)
        .groupBy('capabilityId')
        .map((appCapabilities, capabilityId) => ( {
            capability: capabilitiesById[capabilityId],
            applications: _.map(appCapabilities, appCapability => {
                const application = appsById[appCapability.applicationId];
                return {
                    ...application,
                    primary: appCapability.primary
                };
            })
        } ))
        .value();
}



function extractPrimaryAppIds(appCapabilities) {
    return _.chain(appCapabilities)
        .filter({ primary: true })
        .map('applicationId')
        .uniq()
        .value();
}


function mkIsAppInScopeFn(orgUnitId, apps, appCapabilities) {
    const primaryAppIds = extractPrimaryAppIds(appCapabilities);

    const inScopeAppIds = _.chain(apps)
        .filter(a => options.includeSubUnits ? true : a.organisationalUnitId === orgUnitId)
        .filter(a => options.productionOnly ? a.lifecyclePhase === 'PRODUCTION' : true)
        .filter(a => options.primaryOnly ? _.includes(primaryAppIds, a.id) : true)
        .map('id')
        .value();

    return (id) => _.includes(inScopeAppIds, id);
}


function filterComplexity(complexity, isAppInScope) {
    return _.filter(complexity, c => _.some([
        isAppInScope(c.serverComplexity ? c.serverComplexity.id : 0),
        isAppInScope(c.capabilityComplexity ? c.capabilityComplexity.id : 0),
        isAppInScope(c.connectionComplexity ? c.connectionComplexity.id : 0)
    ]));
}


function filterCapabilityRatings(capabilityRatings, appCapabilities, isAppInScope) {

    const primaryAppCapabilities = _.filter(appCapabilities, 'primary');

    return _.chain(capabilityRatings)
        .filter(r => isAppInScope(r.parent.id))
        .filter(r => {
            if (!options.primaryOnly) {
                return true;
            } else {
                const appId = r.parent.id;
                const capId = r.capability.id;
                const isPrimary =  _.some(
                    primaryAppCapabilities,
                    ac => ac.capabilityId === capId && ac.applicationId === appId);
                return isPrimary;
            }
        })
        .value();
}


function filterEndUserApps(endUserApps, orgUnitId) {
    return _.filter(
        endUserApps,
        eua => options.includeSubUnits ? true : eua.organisationalUnitId === orgUnitId);
}


function filterApps(apps, isAppInScope) {
    return _.filter(apps, a => isAppInScope(a.id))
}


function filterAssetCosts(assetCosts, orgUnitId, isAppInScope) {
    return assetCosts;
    //
    // return _.chain(assetCosts)
    //     .filter(c => isAppInScope(c.application.id))
    //     .filter(c => options.includeSubUnits ? true : c.orgUnit.id === orgUnitId)
    //     .value();
}


function filter(rawData) {

    if (! rawData) { return null; }

    const isAppInScope = mkIsAppInScopeFn(
        rawData.orgUnitId,
        rawData.apps,
        rawData.rawAppCapabilities);

    const apps = filterApps(
        rawData.apps,
        isAppInScope);

    const complexity = filterComplexity(
        rawData.complexity,
        isAppInScope);

    const capabilityRatings = filterCapabilityRatings(
        rawData.capabilityRatings,
        rawData.rawAppCapabilities,
        isAppInScope);

    const rawAppCapabilities = _.filter(
        rawData.rawAppCapabilities,
        ac => isAppInScope(ac.applicationId));

    const endUserApps = filterEndUserApps(
        rawData.endUserApps,
        rawData.orgUnitId);

    const appCapabilities = prepareAppCapabilities(
        rawData.apps,
        rawData.capabilities,
        rawAppCapabilities);

    const assetCosts = filterAssetCosts(
        rawData.assetCosts,
        rawData.orgUnitId,
        isAppInScope);

    return {
        ...rawData,
        appCapabilities,
        endUserApps,
        capabilityRatings,
        complexity,
        apps
    };
}


function service() {
    return {
        options,
        filter
    };
}


export default service;
