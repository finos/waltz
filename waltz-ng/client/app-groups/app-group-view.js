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
import { selectBest } from '../ratings/directives/viewer/coloring-strategies';


function prepareFlowData(flows, apps) {
    const entitiesById = _.indexBy(apps, 'id');

    const enrichedFlows = _.map(flows, f => ({
        source: entitiesById[f.source.id] || { ...f.source, isNeighbour: true },
        target: entitiesById[f.target.id] || { ...f.target, isNeighbour: true },
        dataType: f.dataType
    }));

    const entities = _.chain(enrichedFlows)
        .map(f => ([f.source, f.target]))
        .flatten()
        .uniq(a => a.id)
        .value();

    return {
        flows: enrichedFlows,
        entities
    };
}


function loadDataFlows(dataFlowStore, groupApps) {
    const groupAppIds = _.map(groupApps, 'id');

    return dataFlowStore.findByAppIds(groupAppIds)
        .then(fs => prepareFlowData(fs, groupApps));
}

function calculateRatings(allCapabilities, appCapabilities, ratings) {
    const capabilitiesById = _.indexBy(allCapabilities, 'id');

    const explicitCapabilityIds = _.chain(appCapabilities)
            .map('capabilityId')
            .uniq()
            .value();

    const capabilities = _.chain(explicitCapabilityIds)
        .map(cId => capabilitiesById[cId])
        .compact()
        .map(c => ([c.level1, c.level2, c.level3, c.level4, c.level5]))
        .flatten()
        .compact()
        .uniq()
        .map(cId => capabilitiesById[cId])
        .compact()
        .value();

    return {
        initiallySelectedIds: explicitCapabilityIds,
        explicitCapabilityIds,
        capabilities
    };
}


function controller(appGroupStore,
                    appStore,
                    assetCostStore,
                    complexityStore,
                    dataFlowStore,
                    serverInfoStore,
                    userService,
                    capabilityStore,
                    appCapabilityStore,
                    ratingStore,
                    $stateParams,
                    $q)
{
    const { id }  = $stateParams;

    const vm = this;

    appGroupStore.getById(id)
        .then(groupDetail => vm.groupDetail = groupDetail)
        .then(groupDetail => _.map(groupDetail.applications, 'id'))
        .then(appIds => $q.all([
            appStore.findByIds(appIds),
            assetCostStore.findAppCostsByAppIds(appIds),
            complexityStore.findByAppIds(appIds),
            serverInfoStore.findStatsForAppIds(appIds),
            capabilityStore.findAll(),
            appCapabilityStore.findApplicationCapabilitiesByAppIds(appIds),
            ratingStore.findByAppIds(appIds)
        ]))
        .then(([
            apps,
            assetCosts,
            complexity,
            serverStats,
            allCapabilities,
            appCapabilities,
            ratings
        ]) => {
            vm.applications = apps;
            vm.assetCosts = assetCosts;
            vm.complexity = complexity;
            vm.serverStats = serverStats;
            vm.allCapabilities = allCapabilities;
            vm.appCapabilities = appCapabilities;
            vm.ratings = ratings;
        })
        .then(() => loadDataFlows(dataFlowStore, vm.applications))
        .then(flows => vm.dataFlows = flows)
        .then(() => calculateRatings(vm.allCapabilities, vm.appCapabilities, vm.ratings))
        .then(r => Object.assign(vm, r));

    userService.whoami().then(u => vm.user = u);

    vm.isGroupEditable = () => {
        if (!vm.groupDetail) return false;
        if (!vm.user) return false;
        return _.any(vm.groupDetail.members, m => m.role === 'OWNER' && m.userId === vm.user.userName );
    };

    vm.ratingColorStrategy = selectBest;

}

controller.$inject = [
    'AppGroupStore',
    'ApplicationStore',
    'AssetCostStore',
    'ComplexityStore',
    'DataFlowDataStore',
    'ServerInfoStore',
    'UserService',
    'CapabilityStore',
    'AppCapabilityStore',
    'RatingStore',
    '$stateParams',
    '$q'
];


export default {
    template: require('./app-group-view.html'),
    controller,
    controllerAs: 'ctrl'
};
