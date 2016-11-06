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
    ratings: null,
    rawRatings: [],
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


function nestBySubjectThenMeasurable(ratings) {
    return d3.nest()
        .key(r => r.parent.id)
        .key(r => r.measurableCode)
        .map(ratings);
}

function prepareRawData(apps, measurables, bySubjectThenMeasurable) {
    return _.chain(apps)
        .map(s => ({
            ratings: _.map(
                measurables,
                m => {
                    const ragRating = perhaps(() => bySubjectThenMeasurable[s.id][m.code][0].ragRating, 'Z');
                    return { original: ragRating, current: ragRating, measurable: m.code || m.id };
                }),
            subject: s
        }))
        .sortBy('subject.name')
        .value();
}


function prepareGroupData(capability, apps, perspective, ratings) {

    const measurables = perspective.measurables;
    const bySubjectThenMeasurable = nestBySubjectThenMeasurable(ratings);

    const raw = prepareRawData(
        apps,
        measurables,
        bySubjectThenMeasurable);

    const groupRef = { id: capability.id, name: capability.name, kind: 'CAPABILITY' };

    const summaries = calculateGroupSummary(raw);

    const group = {
        groupRef,
        measurables,
        raw,
        summaries,
        collapsed: false
    };

    return group;
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
                    dataFlowDecoratorStore,
                    entityStatisticStore,
                    historyStore,
                    logicalFlowViewService,
                    perspectiveStore,
                    processStore,
                    physicalFlowLineageStore,
                    ratingStore,
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

    const tweakers = {
        subjectLabel: {
            enter: selection =>
                selection.on('click.go', d => $state.go('main.app.view', { id: d.subject.id }))
        }
    };


    const processApps = (groupedApps) => {
        groupedApps.primaryApps = _.map(groupedApps.primaryApps, a => _.assign(a, {management: 'IT'}));
        groupedApps.secondaryApps = _.map(groupedApps.secondaryApps, a => _.assign(a, {management: 'IT'}));

        const apps = _.union(groupedApps.primaryApps, groupedApps.secondaryApps);
        vm.groupedApps = groupedApps;
        vm.apps = apps;
        return _.map(apps, 'id');
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
        .then(() => {
            $q.all([
                perspectiveStore.findByCode('BUSINESS'),
                ratingStore.findByAppIdSelector(appIdSelector),
                logicalFlowViewService.initialise(capability.id, 'CAPABILITY', 'CHILDREN'),
                complexityStore.findBySelector(capability.id, 'CAPABILITY', 'CHILDREN'),
                assetCostViewService.initialise(appIdSelector, 2016),
                techStatsService.findBySelector(capability.id, 'CAPABILITY', 'CHILDREN'),
                sourceDataRatingStore.findAll()
            ]).then(([
                perspective,
                ratings,
                dataFlows,
                complexity,
                assetCostData,
                techStats,
                sourceDataRatings
            ]) => {
                vm.rawRatings = ratings;
                vm.ratings = {
                    group: prepareGroupData(capability, vm.apps, perspective, ratings),
                    tweakers
                };
                vm.dataFlows = dataFlows;
                vm.complexity = complexity;
                vm.assetCostData = assetCostData;
                vm.techStats = techStats;
                vm.sourceDataRatings = sourceDataRatings;
            });
        })
        .then(() => dataFlowDecoratorStore.findBySelectorAndKind(appIdSelector, 'DATA_TYPE'))
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
    'DataFlowDecoratorStore',
    'EntityStatisticStore',
    'HistoryStore',
    'LogicalFlowViewService',
    'PerspectiveStore',
    'ProcessStore',
    'PhysicalFlowLineageStore',
    'RatingStore',
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
