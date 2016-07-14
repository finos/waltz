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
import {selectBest} from "../ratings/directives/viewer/coloring-strategies";
import {lifecyclePhaseColorScale, variableScale} from "../common/colors";
import {tallyBy} from "../common/tally-utils";


const PIE_SIZE = 70;


function mkChartData(data, groupingField, size, colorProvider = variableScale, labelProvider = null) {
    return {
        config: {
            colorProvider: (d) => colorProvider(d.data.key),
            labelProvider,
            size
        },
        data: tallyBy(data, groupingField)
    };
}



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
    entityStatisticsSummary: [],
    flowOptions: null,
    groupDetail: null,
    initiallySelectedIds: [],
    ratingColorStrategy: selectBest,
    ratings: [],
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
                    appGroupStore,
                    appStore,
                    changeInitiativeStore,
                    complexityStore,
                    dataFlowViewService,
                    entityStatisticStore,
                    userService,
                    capabilityStore,
                    appCapabilityStore,
                    ratingStore,
                    technologyStatsService,
                    assetCostViewService,
                    bookmarkStore,
                    sourceDataRatingStore) {
    const { id }  = $stateParams;

    const vm = Object.assign(this, initialState);


    const appIdSelector = {
        entityReference: {
            kind: 'APP_GROUP',
            id: id
        },
        scope: 'EXACT'
    };

    const isUserAnOwner = member =>
            member.role === 'OWNER'
            && member.userId === vm.user.userName;

    dataFlowViewService.initialise(id, 'APP_GROUP', 'EXACT')
        .then(flows => vm.dataFlows = flows);

    assetCostViewService.initialise(id, 'APP_GROUP', 'EXACT', 2015)
        .then(costs => vm.assetCostData = costs);

    bookmarkStore
        .findByParent({ id , kind: 'APP_GROUP'})
        .then(bookmarks => vm.bookmarks = bookmarks);

    changeInitiativeStore
        .findByRef("APP_GROUP", id)
        .then(list => vm.changeInitiatives = list);


    appGroupStore.getById(id)
        .then(groupDetail => vm.groupDetail = groupDetail)
        .then(groupDetail => _.map(groupDetail.applications, 'id'))
        .then(appIds => $q.all([
            appStore.findByIds(appIds),
            complexityStore.findBySelector(id, 'APP_GROUP', 'EXACT'),
            capabilityStore.findAll(),
            appCapabilityStore.findApplicationCapabilitiesByAppIdSelector(appIdSelector),
            ratingStore.findByAppIdSelector(appIdSelector),
            technologyStatsService.findBySelector(id, 'APP_GROUP', 'EXACT')
        ]))
        .then(([
            apps,
            complexity,
            allCapabilities,
            appCapabilities,
            ratings,
            techStats
        ]) => {
            vm.applications = _.map(apps, a => _.assign(a, {management: 'IT'}));
            vm.complexity = complexity;
            vm.allCapabilities = allCapabilities;
            vm.appCapabilities = appCapabilities;
            vm.ratings = ratings;
            vm.techStats = techStats;

            vm.appSummaryCharts = {
                apps: {
                    byLifecyclePhase: mkChartData(apps, 'lifecyclePhase', PIE_SIZE, lifecyclePhaseColorScale),
                    byKind: mkChartData(apps, 'kind', PIE_SIZE, variableScale)
                }
            };
        })
        .then(() => calculateCapabilities(vm.allCapabilities, vm.appCapabilities))
        .then(result => Object.assign(vm, result))
        .then(() => sourceDataRatingStore.findAll())
        .then((ratings) => vm.sourceDataRatings = ratings);

    userService
        .whoami()
        .then(u => vm.user = u);

    vm.isGroupEditable = () => {
        if (!vm.groupDetail) return false;
        if (!vm.user) return false;
        return _.some(vm.groupDetail.members, isUserAnOwner );
    };

    vm.onAssetBucketSelect = bucket => {
        $scope.$applyAsync(() => {
            assetCostViewService.selectBucket(bucket);
            assetCostViewService.loadDetail()
                .then(data => vm.assetCostData = data);
        })
    };

    vm.loadFlowDetail = () => dataFlowViewService.loadDetail();

    entityStatisticStore.findSummaryStatsByIdSelector(appIdSelector)
        .then(stats => {
            vm.entityStatisticsSummary = stats;
        });

}


controller.$inject = [
    '$scope',
    '$q',
    '$stateParams',
    'AppGroupStore',
    'ApplicationStore',
    'ChangeInitiativeStore',
    'ComplexityStore',
    'DataFlowViewService',
    'EntityStatisticStore',
    'UserService',
    'CapabilityStore',
    'AppCapabilityStore',
    'RatingStore',
    'TechnologyStatisticsService',
    'AssetCostViewService',
    'BookmarkStore',
    'SourceDataRatingStore'
];


export default {
    template: require('./app-group-view.html'),
    controller,
    controllerAs: 'ctrl'
};
