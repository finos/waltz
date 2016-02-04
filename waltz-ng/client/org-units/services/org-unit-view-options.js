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


const options = {
    includeSubUnits: true,
    productionOnly: false,
    primaryOnly: false
};


function prepareAppCapabilities(apps, capabilities, rawAppCapabilities) {
    const appsById = _.indexBy(apps, 'id');
    const capabilitiesById = _.indexBy(capabilities, 'id');

    const appCapabilities =  _.chain(rawAppCapabilities)
        .groupBy(ac => ac.capabilityId)
        .map((appCapabilities, capId) => ( {
            capability: capabilitiesById[capId],
            applications: _.map(appCapabilities, ac => {
                const { primary, applicationId } = ac;
                const app = appsById[applicationId];

                return { ...app, primary };
            })
        } ))
        .value();

    return appCapabilities;
}


function calculateDataFlows(dataFlows, appPredicate) {
    const isFlowInScope = (f) => appPredicate(f.source.id) || appPredicate(f.target.id);

    const flows = _.filter(
        dataFlows.flows,
        isFlowInScope);

    const dataFlowAppIds = _.chain(flows)
        .map(f => ([f.source.id, f.target.id]))
        .flatten()
        .uniq()
        .value();

    const entities = _.filter(
        dataFlows.entities,
        e => _.contains(dataFlowAppIds, e.id));

    return { flows, entities };
}


function extractPrimaryAppIds(appCapabilities) {
    return _.chain(appCapabilities)
        .where({ primary: true })
        .map('applicationId')
        .uniq()
        .value();
}


function mkIsAppInScopeFn(orgUnitId, apps, appCapabilities) {
    const primaryAppIds = extractPrimaryAppIds(appCapabilities);

    const inScopeAppIds = _.chain(apps)
        .filter(a => options.includeSubUnits ? true : a.organisationalUnitId === orgUnitId)
        .filter(a => options.productionOnly ? a.lifecyclePhase === 'PRODUCTION' : true)
        .filter(a => options.primaryOnly ? _.contains(primaryAppIds, a.id) : true)
        .map('id')
        .value();

    return (id) => _.contains(inScopeAppIds, id);
}


function filterComplexity(complexity, isAppInScope) {
    return _.filter(complexity, c => _.any([
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
                const isPrimary =  _.any(
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
    return _.chain(assetCosts)
        .filter(c => isAppInScope(c.application.id))
        .filter(c => options.includeSubUnits ? true : c.orgUnit.id === orgUnitId)
        .value();
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

    const dataFlows = calculateDataFlows(
        rawData.dataFlows,
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

    /*
     orgUnits,
     apps,
     people,
     involvements,
     perspective,
     changeLogs,
     capabilityRatings,
     dataFlows,
     rawAppCapabilities,
     capabilities,
     ratedDataFlows,
     authSources,
     endUserApps,
     assetCosts,
     complexity

     */

    return {
        ...rawData,
        assetCosts,
        appCapabilities,
        endUserApps,
        capabilityRatings,
        dataFlows,
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
