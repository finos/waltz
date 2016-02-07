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

function controller(appGroupStore, appStore, assetCostStore, complexityStore, serverInfoStore, $stateParams, $q) {
    const { id }  = $stateParams;

    const vm = this;

    appGroupStore.getById(id)
        .then(groupDetail => vm.groupDetail = groupDetail)
        .then(groupDetail => _.map(groupDetail.applications, 'id'))
        .then(appIds => $q.all([
            appStore.findByIds(appIds),
            assetCostStore.findAppCostsByAppIds(appIds),
            complexityStore.findByAppIds(appIds),
            serverInfoStore.findStatsForAppIds(appIds)
        ]))
        .then(([
            apps,
            assetCosts,
            complexity,
            serverStats
        ]) => {
            vm.applications = apps;
            vm.assetCosts = assetCosts;
            vm.complexity = complexity;
            vm.serverStats = serverStats;
        })
        .then(() => console.log(vm))



}

controller.$inject = [
    'AppGroupStore',
    'ApplicationStore',
    'AssetCostStore',
    'ComplexityStore',
    'ServerInfoStore',
    '$stateParams',
    '$q'
];


export default {
    template: require('./app-group-view.html'),
    controller,
    controllerAs: 'ctrl'
};
