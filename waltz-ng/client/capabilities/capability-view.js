

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
import d3 from 'd3';

import { perhaps, populateParents } from '../common';
import { calculateGroupSummary } from '../ratings/directives/common';


function logHistory(capability, historyStore) {
    historyStore.put(
        capability.name,
        'CAPABILITY',
        'main.capabilities.view',
        { id: capability.id });
}


function controller(capabilities,
                    appCapabilityStore,
                    perspectiveStore,
                    ratingStore,
                    $q,
                    $stateParams,
                    $state,
                    historyStore,
                    dataFlowStore) {

    const vm = this;

    const capId = Number($stateParams.id);
    const capability = _.findWhere(populateParents(capabilities), { id: capId });

    logHistory(capability, historyStore);

    const capabilitiesById = _.indexBy(capabilities, 'id');

    const applications = [];
    const associatedCapabilities = [];


    appCapabilityStore.findAssociatedApplicationCapabilitiesByCapabilityId(capability.id)
        .then(assocAppCaps => {
            const assocCapabilities = _.chain(assocAppCaps)
                .groupBy('capabilityReference.id')
                .map(associations => ({
                    capabilityReference: associations[0].capabilityReference,
                    apps: _.map(associations, 'applicationReference')
                }))
                .value();

            Object.assign(associatedCapabilities, assocCapabilities);
        });

    $q.all([
        appCapabilityStore.findApplicationsByCapabilityId(capability.id),
        perspectiveStore.findByCode('BUSINESS'),
        ratingStore.findByCapability(capability.id),
        dataFlowStore.findByCapability(capability.id)
    ]).then(([groupedApps, perspective, ratings, dataFlows]) => {

        const apps = _.union(groupedApps.primaryApps, groupedApps.secondaryApps);

        vm.groupedApps = groupedApps;
        vm.apps = apps;

        const measurables = perspective.measurables;
        const bySubjectThenMeasurable = d3.nest()
            .key(r => r.parent.id)
            .key(r => r.measurable.code)
            .map(ratings);

        const raw = _.chain(apps)
            .map(s => ({
                ratings: _.map(
                    measurables,
                    m => {
                        const ragRating = perhaps(() => bySubjectThenMeasurable[s.id][m.code][0].ragRating, 'Z');
                        return { original: ragRating, current: ragRating, measurable: m.code || m.id };
                    }),
                subject: s
            }))
            .sortBy('subject.name')
            .value();

        const group = {
            groupRef: { id: capability.id, name: capability.name, kind: 'CAPABILITY' },
            measurables,
            raw,
            summaries: calculateGroupSummary(raw),
            collapsed: false
        };

        vm.ratings = {
            group,
            tweakers: {
                subjectLabel: {
                    enter: selection =>
                        selection.on('click.go', d => $state.go('main.app-view', { id: d.subject.id }))
                }
            }
        };


        vm.dataFlows = dataFlows;
    });

    vm.capability = capability;
    vm.capabilitiesById = capabilitiesById;
    vm.applications = applications;
    vm.associatedCapabilities = associatedCapabilities;
}

controller.$inject = [
    'capabilities',
    'AppCapabilityStore',
    'PerspectiveStore',
    'RatingStore',
    '$q',
    '$stateParams',
    '$state',
    'HistoryStore',
    'DataFlowDataStore'
];


export default {
    template: require('./capability-view.html'),
    controller,
    controllerAs: 'ctrl'
};
