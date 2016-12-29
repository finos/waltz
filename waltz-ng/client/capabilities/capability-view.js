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

import _ from "lodash";
import {populateParents} from "../common";


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


function controller($q,
                    $scope,
                    $stateParams,
                    appCapabilityStore,
                    applicationStore,
                    assetCostViewService,
                    bookmarkStore,
                    capabilities,
                    changeLogStore,
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

        const appPromise = applicationStore
            .findBySelector(appIdSelector)
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

        const promises = [
            appPromise,
            appCapPromise,
            costPromise,
            complexityPromise
        ];

        return $q.all(promises);
    };


    const wave2 = () => {
        const flowPromise = logicalFlowViewService
            .initialise(capability.id, 'CAPABILITY', 'CHILDREN')
            .then(dataFlows => vm.dataFlows = dataFlows);

        const techPromise = techStatsService
            .findBySelector(capability.id, 'CAPABILITY', 'CHILDREN')
            .then(techStats => vm.techStats = techStats);


        const promises = [
            flowPromise,
            techPromise
        ];

        return $q.all(promises);
    };


    const wave3 = () => {
        const statPromise = entityStatisticStore
            .findAllActiveDefinitions()
            .then(statDefinitions => vm.entityStatisticDefinitions = statDefinitions);

        const physFlowPromise = physicalFlowLineageStore
            .findLineageReportsBySelector(appIdSelector)
            .then(lineageReports => vm.lineageReports = lineageReports);

        const promises = [
            statPromise,
            physFlowPromise
        ];

        return $q.all(promises);

    };

    const wave4 = () => {

        const associatedCapabilitiesPromise = appCapabilityStore
            .findAssociatedApplicationCapabilitiesByCapabilityId(capability.id)
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

        const bookmarkPromise = bookmarkStore
            .findByParent({ id: capId, kind: 'CAPABILITY'})
            .then(bookmarks => vm.bookmarks = bookmarks);

        const tourPromise = tourService
            .initialiseForKey('main.capability.view', true)
            .then(tour => vm.tour = tour);

        const sourceDataRatingsPromise = sourceDataRatingStore
            .findAll()
            .then(sourceDataRatings => vm.sourceDataRatings = sourceDataRatings);

        const processPromise = processStore
            .findForCapability(capId)
            .then(ps => vm.processes = ps);

        const changeLogPromise = changeLogStore
            .findByEntityReference('CAPABILITY', capId)
            .then(changeLogs => vm.changeLogs = changeLogs);

        const promises = [
            bookmarkPromise,
            tourPromise,
            sourceDataRatingsPromise,
            processPromise,
            associatedCapabilitiesPromise,
            changeLogPromise
        ];

        return $q.all(promises);
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

    vm.loadAllCosts = () => {
        $scope.$applyAsync(() => {
            assetCostViewService.loadDetail()
                .then(data => vm.assetCostData = data);
        });
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
    'ChangeLogStore',
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
