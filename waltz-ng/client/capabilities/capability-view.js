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
    visibility: {}
};


function logHistory(capability, historyStore) {
    return historyStore
        .put(capability.name,
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
                    $stateParams,
                    appCapabilityStore,
                    applicationStore,
                    assetCostViewService,
                    bookmarkStore,
                    capabilities,
                    complexityStore,
                    entityStatisticStore,
                    historyStore,
                    logicalFlowViewService,
                    processStore,
                    physicalFlowLineageStore,
                    sourceDataRatingStore,
                    techStatsService,
                    tourService) {

    const vm = Object.assign(this, initialState);

    const capId = $stateParams.id;
    const capability = _.find(populateParents(capabilities), { id: capId });

    const entityReference = {
        kind: 'CAPABILITY',
        id: capId
    };

    const appIdSelector = {
        entityReference,
        scope: 'CHILDREN'
    };


    vm.capability = capability;
    vm.entityRef = entityReference;


    // -- LOADERS --

    const wave1 = () => {
        const appPromise = appCapabilityStore
            .findApplicationsByCapabilityId(capId)
            .then(processApps)
            .then(apps => vm.apps = apps);

        const appCapPromise = appCapabilityStore
            .findByCapabilityIds([capId])
            .then(appCaps => vm.appCapabilities = appCaps);

        const costPromise = assetCostViewService
            .initialise(appIdSelector, 2016)
            .then(costs => vm.assetCostData = costs);

        const complexityPromise = complexityStore
            .findBySelector(capability.id, 'CAPABILITY', 'CHILDREN')
            .then(complexity => vm.complexity = complexity);

        return $q.all([appPromise, appCapPromise, costPromise, complexityPromise]);
    };


    const wave2 = () => {
        const flowPromise = logicalFlowViewService
            .initialise(capability.id, 'CAPABILITY', 'CHILDREN')
            .then(dataFlows => vm.dataFlows = dataFlows);

        const techPromise = techStatsService
            .findBySelector(capability.id, 'CAPABILITY', 'CHILDREN')
            .then(techStats => vm.techStats = techStats);


        return $q.all([flowPromise, techPromise]);
    };


    const wave3 = () => {
        const statPromise = entityStatisticStore
            .findAllActiveDefinitions()
            .then(statDefinitions => vm.entityStatisticDefinitions = statDefinitions);

        const physFlowPromise = physicalFlowLineageStore
            .findLineageReportsBySelector(appIdSelector)
            .then(lineageReports => vm.lineageReports = lineageReports);

        return $q.all([statPromise, physFlowPromise]);

    };

    const wave4 = () => {

        appCapabilityStore.findAssociatedApplicationCapabilitiesByCapabilityId(capability.id)
            .then(assocAppCaps => {
                const capabilitiesById = _.keyBy(capabilities, 'id');
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

        tourService
            .initialiseForKey('main.capability.view', true)
            .then(tour => vm.tour = tour);

        sourceDataRatingStore
            .findAll()
            .then(sourceDataRatings => vm.sourceDataRatings = sourceDataRatings);

        processStore
            .findForCapability(capId)
            .then(ps => vm.processes = ps);
    };


    const postLoad = () => {
        return logHistory(capability, historyStore);
    };


    // -- BOOT ---

    wave1()
        .then(wave2)
        .then(wave3)
        .then(wave4)
        .then(postLoad);



    // -- INTERACT ---

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


}


controller.$inject = [
    '$q',
    '$scope',
    '$stateParams',
    'AppCapabilityStore',
    'ApplicationStore',
    'AssetCostViewService',
    'BookmarkStore',
    'capabilities',
    'ComplexityStore',
    'EntityStatisticStore',
    'HistoryStore',
    'LogicalFlowViewService',
    'ProcessStore',
    'PhysicalFlowLineageStore',
    'SourceDataRatingStore',
    'TechnologyStatisticsService',
    'TourService'
];


export default {
    template: require('./capability-view.html'),
    controller,
    controllerAs: 'ctrl'
};
