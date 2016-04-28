
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
import d3 from "d3";
import EventDispatcher from "../common/EventDispatcher";
import {perhaps} from "../common/index";
import {calculateGroupSummary} from "../ratings/directives/common";


function indexCapabilities(appCapabilities) {
    return _.chain(appCapabilities)
        .map('capability')
        .keyBy('id')
        .value();
}


function calculateGroupData(apps, appCapabilities, measurables, allRatings) {
    const appsById = _.keyBy(apps, 'id');

    const capabilitiesById = indexCapabilities(appCapabilities);

    const byCapabilityThenAppThenMeasurable = d3.nest()
        .key(r => r.capability.id)
        .key(r => r.parent.id)
        .key(r => r.measurable.code)
        .map(allRatings);

    const mkRatings = (appId, rawRatings, capabilityId) => {
        const ratings = _.map(
            measurables,
                m => {
                    const ragRating = perhaps(() => rawRatings[m.code][0].ragRating, 'Z');
                    return { current: ragRating, measurable: m.code || m.id };
                }
        );
        return {
            subject: appsById[appId],
            capabilityId,
            ratings
        };
    };

    const mkGroup = (capabilityId) => {
        const capabilityGroup = byCapabilityThenAppThenMeasurable[capabilityId];

        const raw = _.chain(capabilityGroup)
            .keys()
            .map(appId => mkRatings(appId, capabilityGroup[appId], capabilityId))
            .sortBy('subject.name')
            .value();

        return {
            groupRef: capabilitiesById[capabilityId],
            raw,
            measurables,
            summaries: calculateGroupSummary(raw),
            collapsed: true
        };
    };

    return _.chain(byCapabilityThenAppThenMeasurable)
        .keys()
        .map(mkGroup)
        .sortBy('groupRef.name')
        .value();
}


function calculateCapabilityRatings(orgUnitId, apps, appCapabilities, perspective, allRatings, $state) {

    const measurables = perspective.measurables;
    const groups = calculateGroupData(apps, appCapabilities, measurables, allRatings);

    function isPrimary(app, capabilityId) {
        const c = _.find(appCapabilities, ac => ac.capability.id === Number(capabilityId));
        if (!c) return false;
        const a = _.find(c.applications, { id: app.id });
        if (!a) return false;
        return a.primary;
    }

    const data = {
        groups,
        tweakers: {
            subjectLabel: {
                enter: selection =>
                    selection
                        .on('click.go', d => $state.go('main.app-view', { id: d.subject.id }))
                        .attr('font-style', d => d.subject.organisationalUnitId === orgUnitId ? 'none' : 'italic')
                        .text(d => isPrimary(d.subject, d.capabilityId)
                            ? '\u2605 ' + _.truncate(d.subject.name, 24)
                            : _.truncate(d.subject.name, 26))

            },
            ratingRow: {
                enter: selection =>
                    selection.attr('opacity', d => d.subject.organisationalUnitId === orgUnitId ? 1 : 0.6)
            }
        },
        measurables
    };

    function showDirectsOnly() {
        const directApps = _.filter(apps, { organisationalUnitId: orgUnitId });
        const directAppIds = _.map(directApps, 'id');

        data.groups = calculateGroupData(
            directApps,
            appCapabilities,
            measurables,
            _.filter(allRatings, r => _.includes(directAppIds, r.parent.id)));
    }

    function showAll() {
        data.groups = calculateGroupData(apps, appCapabilities, measurables, allRatings);
    }

    function expandAll() {
        _.map(data.groups, g => g.collapsed = false);
    }

    function collapseAll() {
        _.map(data.groups, g => g.collapsed = true);
    }

    data.actions = {
        expandAll,
        collapseAll,
        showDirectsOnly,
        showAll
    };


    return data;
}


function controller($stateParams,
                    $state,
                    $scope,
                    viewDataService,
                    viewOptions,
                    historyStore) {

    const id = $stateParams.id;
    const vm = this;

    const refresh = () => {
        if (!vm.rawViewData) return;
        const orgUnit = vm.rawViewData.orgUnit;

        historyStore.put(orgUnit.name, 'ORG_UNIT', 'main.org-units.unit', { id: orgUnit.id });

        vm.viewData = viewOptions.filter(vm.rawViewData);
        vm.ratings = calculateCapabilityRatings(
            id,
            vm.viewData.apps,
            vm.viewData.appCapabilities,
            vm.viewData.perspective,
            vm.viewData.capabilityRatings,
            $state);
    };

    $scope.$watch(
        () => viewOptions.options,
        refresh,
        true);

    vm.entityRef = { kind: 'ORG_UNIT', id };

    vm.eventDispatcher = new EventDispatcher();

    viewDataService
        .loadAll(id)
        .then(data => vm.rawViewData = data)
        .then(refresh);

}

controller.$inject = [
    '$stateParams',
    '$state',
    '$scope',
    'OrgUnitViewDataService',
    'OrgUnitViewOptionsService',
    'HistoryStore'
];


export default {
    template: require('./unit-view.html'),
    controller,
    controllerAs: 'ctrl'
};
