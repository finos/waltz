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
    loadInvolvements,
    loadServers,
    loadSoftwareCatalog,
    loadSourceDataRatings
} from "./data-load";
import {prepareSlopeGraph} from "../data-flow/directives/slope-graph/slope-graph-utils";
import {mkAppRatingsGroup, calculateHighestRatingCount} from "../ratings/directives/common";


const initialState = {
    aliases: [],
    appAuthSources: [],
    appCapabilities: [],
    capabilities: [],
    complexity: [],
    databases: [],
    dataTypes: [],
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


function controller($q,
                    $state,
                    appView,
                    aliasStore,
                    authSourcesStore,
                    changeLogStore,
                    complexityStore,
                    databaseStore,
                    dataFlowStore,
                    involvementStore,
                    orgUnitStore,
                    perspectiveStore,
                    processStore,
                    ratingStore,
                    serverInfoStore,
                    softwareCatalogStore,
                    sourceDataRatingStore,
                    displayNameService ) {

    const { id, organisationalUnitId } = appView.app;

    const entityRef = { id, kind: 'APPLICATION' };

    const vm = Object.assign(this, initialState);

    const perspectiveCode = 'BUSINESS';

    $q.all([
        perspectiveStore.findByCode(perspectiveCode),
        ratingStore.findByParentAndPerspective('APPLICATION', id, perspectiveCode)
    ]).then(([perspective, ratings]) => {

        const appRef = { id: id, kind: 'APPLICATION', name: appView.app.name};

        const group = mkAppRatingsGroup(appRef, perspective.measurables, appView.capabilities, ratings);

        vm.ratings = {
            highestRatingCount: calculateHighestRatingCount([group]),
            tweakers: {
                subjectLabel: {
                    enter: (selection) => selection.on('click', (d) => $state.go('main.capability.view', { id: d.subject.id }))
                }
            },
            group
        };

    });


    Object.assign(vm, appView);

    const promises = [
        loadDataFlows(dataFlowStore, id, vm),
        loadInvolvements($q, involvementStore, id, vm),
        loadAuthSources(authSourcesStore, orgUnitStore, id, organisationalUnitId, vm),
        loadServers(serverInfoStore, id, vm),
        loadSoftwareCatalog(softwareCatalogStore, id, vm),
        loadDatabases(databaseStore, id, vm)
    ];

    $q.all(promises)
        .then(() => {
            const graphData = prepareSlopeGraph(
                id,
                vm.flows,
                vm.dataTypes,
                vm.appAuthSources,
                vm.ouAuthSources,
                displayNameService,
                $state);

            vm.flow = graphData;
        })
        .then(() => loadChangeLog(changeLogStore, id, vm))
        .then(() => loadSourceDataRatings(sourceDataRatingStore, vm))


    complexityStore
        .findByApplication(id)
        .then(c => vm.complexity = c);

    processStore
        .findForApplication(id)
        .then(ps => vm.processes = ps);

    vm.saveAliases = (aliases) => {
        const aliasValues = _.map(aliases, 'text');
        return aliasStore
            .update(entityRef, aliasValues)
            .then(() => vm.aliases = aliasValues);
    };

}


controller.$inject = [
    '$q',
    '$state',
    'appView',
    'AliasStore',
    'AuthSourcesStore',
    'ChangeLogDataService',
    'ComplexityStore',
    'DatabaseStore',
    'DataFlowDataStore',
    'InvolvementStore',
    'OrgUnitStore',
    'PerspectiveStore',
    'ProcessStore',
    'RatingStore',
    'ServerInfoStore',
    'SoftwareCatalogStore',
    'SourceDataRatingStore',
    'WaltzDisplayNameService'
];


export default  {
    template: require('./app-view.html'),
    controller,
    controllerAs: 'ctrl'
};

