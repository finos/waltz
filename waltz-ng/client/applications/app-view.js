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
import {mkAppRatingsGroup, calculateHighestRatingCount} from "../ratings/directives/common";


const initialState = {
    app: {},
    aliases: [],
    appAuthSources: [],
    appCapabilities: [],
    capabilities: [],
    complexity: [],
    databases: [],
    dataArticles: [],
    dataTypes: [],
    dataTypeUsages: [],
    explicitTraits: [],
    flows: [],
    log: [],
    ouAuthSources: [],
    organisationalUnit: null,
    peopleInvolvements: [],
    processes: [],
    ratings: null,
    servers: [],
    softwareCatalog: [],
    sourceDataRatings: [],
    tags: [],
    visibility: {}
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
                    appCapabilityStore,
                    assetCostStore,
                    aliasStore,
                    authSourcesStore,
                    bookmarkStore,
                    capabilityStore,
                    changeLogStore,
                    databaseStore,
                    dataFlowStore,
                    dataFlowDecoratorStore,
                    dataTypeUsageStore,
                    entityStatisticStore,
                    historyStore,
                    involvementStore,
                    orgUnitStore,
                    perspectiveStore,
                    physicalDataArticleStore,
                    physicalFlowStore,
                    processStore,
                    ratingStore,
                    serverInfoStore,
                    softwareCatalogStore,
                    sourceDataRatingStore) {

    const id = $stateParams.id;
    const entityRef = { id, kind: 'APPLICATION' };
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
    vm.entityRef = entityRef;
    const perspectiveCode = 'BUSINESS';

    vm.saveAliases = (aliases) => {
        const aliasValues = _.map(aliases, 'text');
        return aliasStore
            .update(entityRef, aliasValues)
            .then(() => vm.aliases = aliasValues);
    };


    function loadAll() {
        loadFirstWave()
            .then(() => loadSecondWave())
            .then(() => loadThirdWave())
            .then(() => loadFourthWave())
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
            appCapabilityStore.findCapabilitiesByApplicationId(id)
                .then(appCapabilities => vm.appCapabilities = appCapabilities),

            capabilityStore.findByAppIds([id])
                .then(capabilities => vm.capabilities = capabilities),

            bookmarkStore.findByParent(entityRef)
                .then(bookmarks => vm.bookmarks = bookmarks)
        ];

        return $q.all(promises);
    }


    function loadThirdWave() {
        const promises = [
            loadDataFlows(dataFlowStore, id, vm),
            loadInvolvements($q, involvementStore, id, vm),
            loadAuthSources(authSourcesStore, orgUnitStore, id, vm.organisationalUnit.id, vm),
            loadServers(serverInfoStore, id, vm),
            loadSoftwareCatalog(softwareCatalogStore, id, vm),
            loadDatabases(databaseStore, id, vm),
            loadDataTypeUsages(dataTypeUsageStore, id, vm),
            loadDataFlowDecorators(dataFlowDecoratorStore, id, vm),

            processStore
                .findForApplication(id)
                .then(ps => vm.processes = ps),

            physicalDataArticleStore
                .findByAppId(id)
                .then(xs => vm.physicalDataArticles = xs),

            physicalFlowStore
                .findByEntityReference(entityRef)
                .then(xs => vm.physicalDataFlows = xs),

            entityStatisticStore
                .findStatsForEntity(entityRef)
                .then(stats => vm.entityStatistics = stats),

            assetCostStore
                .findByAppId(id)
                .then(costs => vm.costs = costs)
        ];

        return $q.all(promises)
            .then(() => loadChangeLog(changeLogStore, id, vm))
            .then(() => loadSourceDataRatings(sourceDataRatingStore, vm));
    }


    function loadFourthWave() {
        return $q.all([
            perspectiveStore.findByCode(perspectiveCode),
            ratingStore.findByParent('APPLICATION', id)
        ]).then(([perspective, ratings]) => {
            const appRef = { id: id, kind: 'APPLICATION', name: vm.app.name};
            const group = mkAppRatingsGroup(appRef, perspective.measurables, vm.capabilities, ratings);

            vm.ratings = {
                highestRatingCount: calculateHighestRatingCount([group]),
                tweakers: {
                    subjectLabel: {
                        enter: (selection) => selection.on(
                            'click',
                            (d) => $state.go('main.capability.view', { id: d.subject.id }))
                    }
                },
                group
            };
        });

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
    'AppCapabilityStore',
    'AssetCostStore',
    'AliasStore',
    'AuthSourcesStore',
    'BookmarkStore',
    'CapabilityStore',
    'ChangeLogDataService',
    'DatabaseStore',
    'DataFlowDataStore',
    'DataFlowDecoratorStore',
    'DataTypeUsageStore',
    'EntityStatisticStore',
    'HistoryStore',
    'InvolvementStore',
    'OrgUnitStore',
    'PerspectiveStore',
    'PhysicalDataArticleStore',
    'PhysicalFlowStore',
    'ProcessStore',
    'RatingStore',
    'ServerInfoStore',
    'SoftwareCatalogStore',
    'SourceDataRatingStore'
];


export default  {
    template: require('./app-view.html'),
    controller,
    controllerAs: 'ctrl'
};

