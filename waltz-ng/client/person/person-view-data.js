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

import _ from 'lodash';


const initModel = {
    managers: [],
    directs: [],
    person: null,
    involvements: {
        direct: [],
        indirect: [],
        allApps: []
    },
    apps: []
};


function service(personStore, involvementStore, assetCostStore, $q) {

    const state = { model: initModel };

    function loadPeople(employeeId) {
        personStore.getByEmployeeId(employeeId)
            .then(person => state.model.person = person);

        personStore.findDirects(employeeId)
            .then(directs => state.model.directs = directs);

        personStore.findManagers(employeeId)
            .then(managers => state.model.managers = managers);
    }

    function loadApplications(employeeId) {
        return $q.all([
            involvementStore.findByEmployeeId(employeeId),
            involvementStore.findAppsForEmployeeId(employeeId)
        ]).then(([involvements, apps]) => {
            const appsById = _.indexBy(apps, 'id');

            const appInvolvements = _.filter(involvements, i => i.entityReference.kind === 'APPLICATION');
            const directlyInvolvedAppIds = _.map(appInvolvements, 'entityReference.id');

            const allAppIds = _.map(apps, 'id');
            const indirectlyInvolvedAppIds = _.difference(allAppIds, directlyInvolvedAppIds);

            const directAppInvolvements = _.chain(appInvolvements)
                .groupBy('kind')
                .map((vs, k) => ({ kind: k, apps: _.map(vs, v => appsById[v.entityReference.id])}))
                .value();

            const indirectAppInvolvements = _.map(indirectlyInvolvedAppIds, id => appsById[id]);

            const summary = {
                direct: directAppInvolvements,
                indirect: indirectAppInvolvements,
                allApps: apps
            };

            state.model.apps = apps;
            state.model.involvements = summary;

            return state.model;

        });
    }


    function loadCosts(model) {
        const appIds = _.map(model.apps, 'id');
        assetCostStore.findAppCostsByAppIds(appIds).then(assetCosts => model.assetCosts = assetCosts);
    }


    function load(employeeId) {
        loadPeople(employeeId);
        loadApplications(employeeId)
            .then(loadCosts);
    }


    return {
        load,
        state
    };
}

service.$inject = [
    'PersonDataService',
    'InvolvementDataService',
    'AssetCostStore',
    '$q'
];

export default service;

