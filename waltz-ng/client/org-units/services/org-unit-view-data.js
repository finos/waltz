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

import _ from 'lodash';

import RatedFlowsData from '../../data-flow/RatedFlowsData';
import { aggregatePeopleInvolvements } from '../../involvement/involvement-utils';


function prepareFlowData(flows, apps) {
    const entitiesById = _.indexBy(apps, 'id');

    const enrichedFlows = _.map(flows, f => ({
        source: entitiesById[f.source.id],
        target: entitiesById[f.target.id],
        dataType: f.dataType
    }));

    return {
        flows: enrichedFlows,
        entities: apps
    };
}


function loadDataFlows(dataFlowStore, appStore, groupApps) {
    const groupAppIds = _.map(groupApps, 'id');

    return dataFlowStore.findByAppIds(groupAppIds)
        .then(fs => {

            const allAppIds = _.chain(fs)
                .map(f => ([f.source.id, f.target.id]))
                .flatten()
                .uniq()
                .value();

            const neighbourIds = _.difference(allAppIds, groupApps);

            return appStore
                .findByIds(neighbourIds)
                .then(neighbourApps => _.map(neighbourApps, a => ({...a, isNeighbour: true })))
                .then(neighbourApps => prepareFlowData(fs, _.union(groupApps, neighbourApps)));
        });
}


function loadAppCapabilities(appCapabilityStore, id) {
    return appCapabilityStore
        .findApplicationCapabilitiesForOrgUnitTree(id)
        .then(rawAppCapabilities => {
            const capabilitiesById = _.chain(rawAppCapabilities)
                .map('capabilityReference')
                .uniq('id')
                .indexBy('id')
                .value();

            return _.chain(rawAppCapabilities)
                .groupBy(ac => ac.capabilityReference.id)
                .map((apps, cap) => ( {
                    capability: capabilitiesById[cap],
                    applications: _.map(apps, ac => {
                        const { applicationReference, isPrimary } = ac;
                        return { ...applicationReference, isPrimary };
                    })
                } ))
                .value();
        });
}


function calculateDataFlows(dataFlows, appPredicate) {
    const flows = _.filter(dataFlows.flows,
            f => appPredicate(f.source.id) || appPredicate(f.target.id));

    const dataFlowAppIds = _.chain(flows)
        .map(f => ([f.source.id, f.target.id]))
        .flatten()
        .uniq()
        .value();

    const entities = _.filter(dataFlows.entities, e => _.contains(dataFlowAppIds, e.id));

    return { flows, entities };
}


function extractPrimaryAppIds(appCapabilities) {
    return _.chain(appCapabilities)
        .map('applications')
        .flatten()
        .where({ isPrimary: true })
        .map('id')
        .value();
}


function categorizeCostsIntoBuckets(costs) {

    // bucketPredicate:: bucket -> amount -> bool
    const bucketPredicate = b => a => (a >= b.min) && (a < b.max);

    const buckets = [
        { min: 0, max: 1000, name: '€0 < 1K', idx: 0, size: 0},
        { min: 1000, max: 5000, name: '€1K < 5K', idx: 1, size: 0},
        { min: 5000, max: 10000, name: '€5K < 10K', idx: 2, size: 0},
        { min: 10000, max: 50000, name: '€10K < 50K', idx: 3, size: 0},
        { min: 50000, max: 100000, name: '€50K < 100K', idx: 4, size: 0},
        { min: 100000, max: 500000, name: '€100K < 500K', idx: 5, size: 0},
        { min: 500000, max: 1000000, name: '€500K < 1M', idx: 6, size: 0},
        { min: 1000000, max: Number.MAX_VALUE, name: '€1M +', idx: 7, size: 0}
    ];

    const findBucket = (c) => {
        return _.find(buckets, b => bucketPredicate(b)(c));
    };


    _.each(costs, c => {
        const bucket = findBucket(c.cost.amount);
        if (bucket) {
            bucket.size++;
        } else {
            console.log('failed to find bucket for ', c);
        }
    });

    return buckets;

}

function service(appStore,
                 appCapabilityStore,
                 orgUnitUtils,
                 changeLogStore,
                 dataFlowStore,
                 involvementStore,
                 ratingStore,
                 perspectiveStore,
                 orgUnitStore,
                 ratedDataFlowDataService,
                 authSourceCalculator,
                 endUserAppStore,
                 assetCostStore,
                 complexityStore,
                 $q) {

    const data = {};

    function loadAll(orgUnitId) {
        return appStore.findByOrgUnitTree(orgUnitId)
            .then(apps => loadAll2(orgUnitId, apps));
    }

    function loadAll2(orgUnitId, apps) {

        const appIds = _.map(apps, 'id');

        return $q.all([
            ratingStore.findByAppIds(appIds),
            loadDataFlows(dataFlowStore, appStore, apps),
            loadAppCapabilities(appCapabilityStore, orgUnitId),
            orgUnitStore.findAll(),
            changeLogStore.findByEntityReference('ORG_UNIT', orgUnitId),
            involvementStore.findPeopleByEntityReference('ORG_UNIT', orgUnitId),
            involvementStore.findByEntityReference('ORG_UNIT', orgUnitId),
            perspectiveStore.findByCode('BUSINESS'),
            ratedDataFlowDataService.findByOrgUnitTree(orgUnitId),
            authSourceCalculator.findByOrgUnit(orgUnitId),
            endUserAppStore.findByOrgUnitTree(orgUnitId),
            assetCostStore.findAppCostsByAppIds(appIds),
            complexityStore.findByOrgUnitTree(orgUnitId)
    ]).then(([
            capabilityRatings,
            dataFlows,
            appCapabilities,
            orgUnits,
            changeLogs,
            people,
            involvements,
            perspective,
            ratedDataFlows,
            authSources,
            endUserApps,
            assetCosts,
            complexity
        ]) => {
            data.assetCosts = assetCosts;
            data.immediateHierarchy = orgUnitUtils.getImmediateHierarchy(orgUnits, orgUnitId);
            data.apps = apps;
            data.logEntries = changeLogs;
            data.people = people;
            data.involvements = aggregatePeopleInvolvements(involvements, people);
            data.perspective = perspective;
            data.capabilityRatings = capabilityRatings;
            data.dataFlows = dataFlows;
            data.appCapabilities = appCapabilities;
            data.orgUnit = _.find(orgUnits, { id: orgUnitId });
            data.ratedFlows = new RatedFlowsData(ratedDataFlows, apps, orgUnits, orgUnitId);
            data.authSources = authSources;
            data.orgUnits = orgUnits;
            data.endUserApps = endUserApps;

            data.assetCostBuckets = categorizeCostsIntoBuckets(assetCosts);
            data.complexity = complexity;

            data.filter = (config) => {

                const primaryApps = extractPrimaryAppIds(appCapabilities);

                const inScopeAppIds = _.chain(apps)
                        .filter(a => config.includeSubUnits ? true : a.organisationalUnitId === orgUnitId)
                        .filter(a => config.productionOnly ? a.lifecyclePhase === 'PRODUCTION' : true)
                        .filter(a => config.primaryOnly ? _.contains(primaryApps, a.id) : true)
                        .map('id')
                        .value();

                const isAppInScope = (id) => _.contains(inScopeAppIds, id);

                data.apps = _.filter(apps, a => isAppInScope(a.id));

                data.complexity = _.filter(complexity, c => _.any([
                    isAppInScope(c.serverComplexity ? c.serverComplexity.id : 0),
                    isAppInScope(c.capabilityComplexity ? c.capabilityComplexity.id : 0),
                    isAppInScope(c.connectionComplexity ? c.connectionComplexity.id : 0)
                ]));

                data.dataFlows = calculateDataFlows(dataFlows, isAppInScope);

                data.capabilityRatings = _.chain(capabilityRatings)
                        .filter(r => isAppInScope(r.parent.id))
                        .filter(r => {
                            if (!config.primaryOnly) {
                                return true;
                            } else {
                                const appId = r.parent.id;
                                const capId = r.capability.id;
                                return _.chain(appCapabilities)
                                    .filter(ac => ac.capability.id === capId)
                                    .map('applications')
                                    .flatten()
                                    .filter('isPrimary')
                                    .map('id')
                                    .contains(appId)
                                    .value();
                            }
                        })
                        .value();

                data.appCapabilities = _.filter(appCapabilities,
                        ac => _.any(ac.applications, a => isAppInScope(a.id)));

                data.endUserApps = _.filter(endUserApps, eua => config.includeSubUnits ? true : eua.organisationalUnitId === orgUnitId);

            };

            return data;
        });
    }


    return {
        loadAll
    };

}

service.$inject = [
    'ApplicationStore',
    'AppCapabilityStore',
    'OrgUnitUtilityService',
    'ChangeLogDataService',
    'DataFlowDataStore',
    'InvolvementDataService',
    'RatingStore',
    'PerspectiveStore',
    'OrgUnitStore',
    'RatedDataFlowDataService',
    'AuthSourcesCalculator',
    'EndUserAppStore',
    'AssetCostStore',
    'ComplexityStore',
    '$q'
];

export default service;
