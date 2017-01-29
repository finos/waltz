/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";
import {
    loadAuthSources,
    loadChangeLog,
    loadDatabases,
    loadDataFlows,
    loadDataFlowDecorators,
    loadDataTypeUsages,
    loadInvolvements,
    loadServers,
    loadSoftwareCatalog,
    loadSourceDataRatings
} from "./data-load";


const initialState = {
    app: {},
    aliases: [],
    appAuthSources: [],
    complexity: [],
    databases: [],
    specifications: [],
    dataTypes: [],
    dataTypeUsages: [],
    flows: [],
    log: [],
    ouAuthSources: [],
    organisationalUnit: null,
    peopleInvolvements: [],
    servers: [],
    softwareCatalog: [],
    sourceDataRatings: [],
    tags: [],
    visibility: {},
    physicalFlowsProducesCount: 0,
    physicalFlowsConsumesCount: 0,
    physicalFlowsUnusedSpecificationsCount: 0,
    physicalFlowProducesExportFn: () => {},
    physicalFlowConsumesExportFn: () => {},
    physicalFlowUnusedSpecificationsExportFn: () => {}
};


const addToHistory = (historyStore, app) => {
    if (! app) { return; }
    historyStore.put(
        app.name,
        'APPLICATION',
        'main.app.view',
        { id: app.id });
};


function controller($q,
                    $state,
                    $stateParams,
                    appViewStore,
                    assetCostStore,
                    aliasStore,
                    authSourcesStore,
                    bookmarkStore,
                    changeLogStore,
                    databaseStore,
                    dataTypeUsageStore,
                    entityStatisticStore,
                    historyStore,
                    involvementStore,
                    logicalFlowDecoratorStore,
                    logicalFlowStore,
                    measurableStore,
                    measurableCategoryStore,
                    measurableRatingStore,
                    orgUnitStore,
                    physicalSpecificationStore,
                    physicalFlowStore,
                    serverInfoStore,
                    softwareCatalogStore,
                    sourceDataRatingStore) {

    const id = $stateParams.id;
    const entityReference = { id, kind: 'APPLICATION' };
    const vm = Object.assign(this, initialState);

    const goToAppFn = d => $state.go('main.app.view', { id: d.id });
    vm.flowTweakers = {
        source: {
            onSelect: goToAppFn,
        },
        target: {
            onSelect: goToAppFn,
        }
    };
    vm.entityRef = entityReference;

    vm.saveAliases = (aliases) => {
        const aliasValues = _.map(aliases, 'text');
        return aliasStore
            .update(entityReference, aliasValues)
            .then(() => vm.aliases = aliasValues);
    };

    vm.onPhysicalFlowsInitialise = (e) => {
        vm.physicalFlowProducesExportFn = e.exportProducesFn;
        vm.physicalFlowConsumesExportFn = e.exportConsumesFn;
        vm.physicalFlowUnusedSpecificationsExportFn = e.exportUnusedSpecificationsFn;
    };

    vm.onPhysicalFlowsChange = (e) => {
        vm.physicalFlowsProducesCount = e.producesCount;
        vm.physicalFlowsConsumesCount = e.consumesCount;
        vm.physicalFlowsUnusedSpecificationsCount = e.unusedSpecificationsCount;
    };

    vm.exportPhysicalFlowProduces = () => {
        vm.physicalFlowProducesExportFn();
    };

    vm.exportPhysicalFlowConsumes = () => {
        vm.physicalFlowConsumesExportFn();
    };

    vm.exportPhysicalFlowUnusedSpecifications = () => {
        vm.physicalFlowUnusedSpecificationsExportFn();
    };


    function loadAll() {
        loadFirstWave()
            .then(() => loadSecondWave())
            .then(() => loadThirdWave())
            .then(() => postLoadActions());
    }


    function loadFirstWave() {
        const promises = [
            appViewStore.getById(id)
                .then(appView => Object.assign(vm, appView)),
        ];

        return $q.all(promises);
    }


    function loadSecondWave() {
        const promises = [
            bookmarkStore.findByParent(entityReference)
                .then(bookmarks => vm.bookmarks = bookmarks),

            measurableCategoryStore
                .findAll()
                .then(cs => vm.measurableCategories = cs),

            measurableRatingStore
                .findByAppSelector({ entityReference, scope: 'EXACT' })
                .then(rs => vm.ratings = rs),

            measurableStore
                .findMeasurablesRelatedToPath(entityReference)
                .then(ms => vm.measurables = ms)
        ];

        return $q.all(promises);
    }


    function loadThirdWave() {
        const promises = [
            loadDataFlows(logicalFlowStore, id, vm),
            loadInvolvements($q, involvementStore, id, vm),
            loadAuthSources(authSourcesStore, orgUnitStore, id, vm.organisationalUnit.id, vm),
            loadServers(serverInfoStore, id, vm),
            loadSoftwareCatalog(softwareCatalogStore, id, vm),
            loadDatabases(databaseStore, id, vm),
            loadDataTypeUsages(dataTypeUsageStore, id, vm),
            loadDataFlowDecorators(logicalFlowDecoratorStore, id, vm),

            physicalSpecificationStore
                .findByEntityReference(entityReference)
                .then(xs => vm.physicalSpecifications = xs),

            physicalFlowStore
                .findByEntityReference(entityReference)
                .then(xs => vm.physicalFlows = xs),

            entityStatisticStore
                .findStatsForEntity(entityReference)
                .then(stats => vm.entityStatistics = stats),

            assetCostStore
                .findByAppId(id)
                .then(costs => vm.costs = costs)
        ];

        return $q.all(promises)
            .then(() => loadChangeLog(changeLogStore, entityReference, vm))
            .then(() => loadSourceDataRatings(sourceDataRatingStore, vm))
    }

    function postLoadActions() {
        addToHistory(historyStore, vm.app);
    }

    // load everything in priority order
    loadAll();
}


controller.$inject = [
    '$q',
    '$state',
    '$stateParams',
    'ApplicationViewStore',
    'AssetCostStore',
    'AliasStore',
    'AuthSourcesStore',
    'BookmarkStore',
    'ChangeLogStore',
    'DatabaseStore',
    'DataTypeUsageStore',
    'EntityStatisticStore',
    'HistoryStore',
    'InvolvementStore',
    'LogicalFlowDecoratorStore',
    'LogicalFlowStore',
    'MeasurableStore',
    'MeasurableCategoryStore',
    'MeasurableRatingStore',
    'OrgUnitStore',
    'PhysicalSpecificationStore',
    'PhysicalFlowStore',
    'ServerInfoStore',
    'SoftwareCatalogStore',
    'SourceDataRatingStore'
];


export default  {
    template: require('./app-view.html'),
    controller,
    controllerAs: 'ctrl'
};

