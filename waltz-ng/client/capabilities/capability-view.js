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

import _ from "lodash";
import d3 from "d3";
import {perhaps, populateParents} from "../common";
import {calculateGroupSummary} from "../ratings/directives/common";


const initialState = {
    apps: [],
    assetCostData: null,
    assetCosts: null,
    associatedCapabilities: [],
    bookmarks: [],
    capability: null,
    complexity: [],
    dataFlows: null,
    entityStatisticDefinitions: [],
    groupedApps: null,
    processes: [],
    sourceDataRatings: [],
    techStats: null,
    traitInfo: null,
    visibility: {}
};


function loadTraitInfo(traitStore, traitUsageStore, capabilityId) {
    const result = {
        usages: [],
        traits: []
    };

    return traitUsageStore
        .findByEntityReference('CAPABILITY', capabilityId)
        .then(usages => {
            if (! usages) { return result; } // shortcut

            result.usages = usages;
            const traitIds = _.chain(usages)
                .map('traitId')
                .uniq()
                .value();

            return traitStore.findByIds(traitIds)
                .then(traits => result.traits = traits)
                .then(() => result);
        });
}


function logHistory(capability, historyStore) {
    historyStore.put(
        capability.name,
        'CAPABILITY',
        'main.capability.view',
        { id: capability.id });
}


function processApps(groupedApps = { primaryApps: [], secondaryApps: []}) {
    const primaryApps = _.map(groupedApps.primaryApps, a => _.assign(a, {management: 'IT'}));
    const secondaryApps = _.map(groupedApps.secondaryApps, a => _.assign(a, {management: 'IT'}));

    return _.union(primaryApps, secondaryApps);
}


function controller($q,
                    $scope,
                    $state,
                    $stateParams,
                    appCapabilityStore,
                    applicationStore,
                    assetCostViewService,
                    bookmarkStore,
                    capabilities,
                    complexityStore,
                    entityStatisticStore,
                    historyStore,
                    logicalFlowDecoratorStore,
                    logicalFlowViewService,
                    processStore,
                    physicalFlowLineageStore,
                    sourceDataRatingStore,
                    techStatsService,
                    tourService,
                    traitStore,
                    traitUsageStore) {

    const vm = Object.assign(this, initialState);

    const capId = $stateParams.id;
    const capability = _.find(populateParents(capabilities), { id: capId });

    const capabilitiesById = _.keyBy(capabilities, 'id');

    const assetCosts = {
        stats: {},
        costs: [],
        loading: false
    };

    const appIdSelector = {
        entityReference: {
            kind: 'CAPABILITY',
            id: capId
        },
        scope: 'CHILDREN'
    };

    vm.entityRef = appIdSelector.entityReference;

    processStore
        .findForCapability(capId)
        .then(ps => vm.processes = ps);

    appCapabilityStore.findApplicationsByCapabilityId(capability.id)
        .then(processApps)
        .then(apps => {
            $q.all([
                appCapabilityStore.findByCapabilityIds([capId]),
                logicalFlowViewService.initialise(capability.id, 'CAPABILITY', 'CHILDREN'),
                complexityStore.findBySelector(capability.id, 'CAPABILITY', 'CHILDREN'),
                assetCostViewService.initialise(appIdSelector, 2016),
                techStatsService.findBySelector(capability.id, 'CAPABILITY', 'CHILDREN'),
                sourceDataRatingStore.findAll()
            ]).then(([
                appCapabilities,
                dataFlows,
                complexity,
                assetCostData,
                techStats,
                sourceDataRatings
            ]) => {
                vm.apps = apps;
                vm.appCapabilities = appCapabilities;
                vm.dataFlows = dataFlows;
                vm.complexity = complexity;
                vm.assetCostData = assetCostData;
                vm.techStats = techStats;
                vm.sourceDataRatings = sourceDataRatings;
            });
        })
        .then(() => logicalFlowDecoratorStore.findBySelectorAndKind(appIdSelector, 'DATA_TYPE'))
        .then((flowDecorators => vm.dataFlowDecorators = flowDecorators))
        .then(() => tourService.initialiseForKey('main.capability.view', true))
        .then(tour => vm.tour = tour);


    appCapabilityStore.findAssociatedApplicationCapabilitiesByCapabilityId(capability.id)
        .then(assocAppCaps => {
            const associatedAppIds = _.map(assocAppCaps, 'applicationId');
            applicationStore
                .findByIds(associatedAppIds)
                .then((assocApps) => {
                    const appsById = _.keyBy(assocApps, 'id');
                    return _.chain(assocAppCaps)
                        .groupBy('capabilityId')
                        .map((associations, capabilityId) => {
                            return {
                                capability: capabilitiesById[capabilityId],
                                apps: _.map(associations, assoc => appsById[assoc.applicationId])
                            }
                        })
                        .value()
                })
                .then(associatedCapabilities => vm.associatedCapabilities = associatedCapabilities);
        });

    bookmarkStore
        .findByParent({ id: capId, kind: 'CAPABILITY'})
        .then(bookmarks => vm.bookmarks = bookmarks);

    logHistory(capability, historyStore);


    vm.capability = capability;
    vm.assetCosts = assetCosts;

    vm.onAssetBucketSelect = bucket => {
        $scope.$applyAsync(() => {
            assetCostViewService.selectBucket(bucket);
            assetCostViewService.loadDetail()
                .then(data => vm.assetCostData = data);
        })
    };

    vm.lineageTableInitialised = (api) => {
        vm.exportLineageReports = api.export;
    };

    vm.loadFlowDetail = () => logicalFlowViewService
        .loadDetail()
        .then(flowData => vm.dataFlows = flowData);


    loadTraitInfo(traitStore, traitUsageStore, capability.id)
        .then(r => vm.traitInfo = r);

    entityStatisticStore
        .findAllActiveDefinitions()
        .then(defns => vm.entityStatisticDefinitions = defns);

    physicalFlowLineageStore
        .findLineageReportsBySelector(appIdSelector)
        .then(lineageReports => vm.lineageReports = lineageReports);
}


controller.$inject = [
    '$q',
    '$scope',
    '$state',
    '$stateParams',
    'AppCapabilityStore',
    'ApplicationStore',
    'AssetCostViewService',
    'BookmarkStore',
    'capabilities',
    'ComplexityStore',
    'EntityStatisticStore',
    'HistoryStore',
    'LogicalFlowDecoratorStore',
    'LogicalFlowViewService',
    'ProcessStore',
    'PhysicalFlowLineageStore',
    'SourceDataRatingStore',
    'TechnologyStatisticsService',
    'TourService',
    'TraitStore',
    'TraitUsageStore'
];


export default {
    template: require('./capability-view.html'),
    controller,
    controllerAs: 'ctrl'
};
