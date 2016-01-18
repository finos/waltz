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


function addDirects(model, directs) {
    return _.set(model, ['directs'], directs);
}


function addManagers(model, managers) {
    return _.set(model, ['managers'], managers);
}


function addPerson(model, person) {
    return _.set(model, ['person'], person);
}


function addInvolvements(model, involvements) {
    return _.set(model, ['involvements'], involvements);
}


const initModel = {
    managers: [],
    directs: [],
    person: null,
    involvements: {
        direct: [],
        indirect: [],
        allApps: []
    }
};


function service(personStore, involvementStore, $q) {

    const state = { model: initModel };

    function loadPeople(employeeId) {
        personStore.getByEmployeeId(employeeId)
            .then(person => state.model = addPerson(state.model, person));

        personStore.findDirects(employeeId)
            .then(directs => state.model = addDirects(state.model, directs));

        personStore.findManagers(employeeId)
            .then(managers => state.model = addManagers(state.model, managers));
    }

    function loadApplications(employeeId) {
        $q.all([
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

            state.model = addInvolvements(state.model, summary);

            //vm.allRoles = _.map(vm.appInvolvements.direct, 'kind');
        });
    }

    function load(employeeId) {
        loadPeople(employeeId);
        loadApplications(employeeId);
    }

    return {
        load,
        state
    };
}

service.$inject = [
    'PersonDataService',
    'InvolvementDataService',
    '$q'
];

export default service;

