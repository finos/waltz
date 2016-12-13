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


/**
 * Calculates the set of capabilities required to
 * fully describe a given set of app capabilities.
 * This may include parent capabilities of the
 * explicit capabilities.
 *
 * @param appCapabilities
 * @param allCapabilities
 * @returns {*}
 */
function includeParentCapabilities(appCapabilities, allCapabilities) {
    const capabilitiesById = _.keyBy(allCapabilities, 'id');

    const toCapabilityId = appCap => appCap.capabilityId;
    const lookupCapability = id => capabilitiesById[id];
    const extractParentIds = c => ([c.level1, c.level2, c.level3, c.level4, c.level5]);

    return _.chain(appCapabilities)
        .map(toCapabilityId)
        .map(lookupCapability)
        .map(extractParentIds)
        .flatten()
        .compact()
        .uniq()
        .map(lookupCapability)
        .compact()
        .value();
}


function calculateCapabilities(allCapabilities, appCapabilities) {
    const explicitCapabilityIds = _.chain(appCapabilities)
            .map('capabilityId')
            .uniq()
            .value();

    const capabilities = includeParentCapabilities(appCapabilities, allCapabilities);

    return {
        initiallySelectedIds: explicitCapabilityIds,
        explicitCapabilityIds,
        capabilities
    };
}


const initialState = {
    allCapabilities: [],
    appCapabilities: [],
    applications: [],
    assetCostData: null,
    bookmarks: [],
    capabilities: [],
    changeInitiatives: [],
    complexity: [],
    dataFlows : null,
    entityStatisticDefinitions: [],
    flowOptions: null,
    groupDetail: null,
    initiallySelectedIds: [],
    sourceDataRatings: [],
    techStats: null,
    user: null,
    visibility: {
        applicationOverlay: false,
        bookmarkOverlay: false,
        capabilityRatingOverlay: false,
        changeInitiativeOverlay: false,
        costOverlay: false,
        flowOverlay: false,
        techOverlay: false
    }
};


function controller($scope,
                    $q,
                    $stateParams,
                    appCapabilityStore,
                    appGroupStore,
                    appStore,
                    assetCostViewService,
                    bookmarkStore,
                    capabilityStore,
                    changeInitiativeStore,
                    changeLogStore,
                    complexityStore,
                    entityStatisticStore,
                    historyStore,
                    logicalFlowViewService,
                    physicalFlowLineageStore,
                    sourceDataRatingStore,
                    technologyStatsService,
                    userService) {
    const { id }  = $stateParams;


    const vm = Object.assign(this, initialState);


    const appIdSelector = {
        entityReference: {
            kind: 'APP_GROUP',
            id: id
        },
        scope: 'EXACT'
    };

    vm.entityRef = appIdSelector.entityReference;

    const isUserAnOwner = member =>
            member.role === 'OWNER'
            && member.userId === vm.user.userName;

    logicalFlowViewService
        .initialise(id, 'APP_GROUP', 'EXACT')
        .then(flows => vm.dataFlows = flows);

    assetCostViewService
        .initialise(appIdSelector, 2016)
        .then(costs => vm.assetCostData = costs);

    bookmarkStore
        .findByParent({ id , kind: 'APP_GROUP'})
        .then(bookmarks => vm.bookmarks = bookmarks);

    changeInitiativeStore
        .findByRef("APP_GROUP", id)
        .then(list => vm.changeInitiatives = list);


    appGroupStore.getById(id)
        .then(groupDetail => vm.groupDetail = groupDetail)
        .then(groupDetail => {
            historyStore.put(groupDetail.appGroup.name, 'APP_GROUP', 'main.app-group.view', { id });
            return groupDetail;
        })
        .then(groupDetail => _.map(groupDetail.applications, 'id'))
        .then(appIds => $q.all([
            appStore.findByIds(appIds),
            complexityStore.findBySelector(id, 'APP_GROUP', 'EXACT'),
            capabilityStore.findAll(),
            appCapabilityStore.findApplicationCapabilitiesByAppIdSelector(appIdSelector),
            technologyStatsService.findBySelector(id, 'APP_GROUP', 'EXACT')
        ]))
        .then(([
            apps,
            complexity,
            allCapabilities,
            appCapabilities,
            techStats
        ]) => {
            vm.applications = _.map(apps, a => _.assign(a, {management: 'IT'}));
            vm.complexity = complexity;
            vm.allCapabilities = allCapabilities;
            vm.appCapabilities = appCapabilities;
            vm.techStats = techStats;
        })
        .then(() => calculateCapabilities(vm.allCapabilities, vm.appCapabilities))
        .then(result => Object.assign(vm, result))
        .then(() => sourceDataRatingStore.findAll())
        .then((sourceDataRatings) => vm.sourceDataRatings = sourceDataRatings)
        .then(() => changeLogStore.findByEntityReference('APP_GROUP', id))
        .then(changeLogs => vm.changeLogs = changeLogs);

    userService
        .whoami()
        .then(u => vm.user = u);

    vm.isGroupEditable = () => {
        if (!vm.groupDetail) return false;
        if (!vm.user) return false;
        return _.some(vm.groupDetail.members, isUserAnOwner );
    };

    vm.loadAllCosts = () => {
        $scope.$applyAsync(() => {
            assetCostViewService.loadDetail()
                .then(data => vm.assetCostData = data);
        })
    };

    vm.loadFlowDetail = () => logicalFlowViewService
        .loadDetail()
        .then(flowData => vm.dataFlows = flowData);

    entityStatisticStore
        .findAllActiveDefinitions()
        .then(definitions => vm.entityStatisticDefinitions = definitions);

    vm.lineageTableInitialised = (api) => {
        vm.exportLineageReports = api.export;
    };

    physicalFlowLineageStore
        .findLineageReportsBySelector(appIdSelector)
        .then(lineageReports => vm.lineageReports = lineageReports);

}


controller.$inject = [
    '$scope',
    '$q',
    '$stateParams',
    'AppCapabilityStore',
    'AppGroupStore',
    'ApplicationStore',
    'AssetCostViewService',
    'BookmarkStore',
    'CapabilityStore',
    'ChangeInitiativeStore',
    'ChangeLogStore',
    'ComplexityStore',
    'EntityStatisticStore',
    'HistoryStore',
    'LogicalFlowViewService',
    'PhysicalFlowLineageStore',
    'SourceDataRatingStore',
    'TechnologyStatisticsService',
    'UserService'
];


export default {
    template: require('./app-group-view.html'),
    controller,
    controllerAs: 'ctrl'
};
