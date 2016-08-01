
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
import {calculateGroupSummary, determineChanges, mkAppRatingsGroup} from "../ratings/directives/common";


function createChangeAction(group, app, perspectiveCode) {
    const toChangeAction = (c) => ({
        original: c.original,
        current: c.current,
        measurable: { code: c.measurable.code, name: c.measurable.name },
        capability: { id: c.subject.id, kind: 'CAPABILITY', name: c.subject.name }
    });

    const changes = _.map(determineChanges(group), toChangeAction);

    const action = {
        application: { kind: 'APPLICATION', id: app.id, name: app.name },
        perspectiveCode,
        changes
    };

    return action;
}


function controller(appStore,
                    appCapabilityStore,
                    perspectiveStore,
                    ratingStore,
                    capabilityStore,
                    notification,
                    $q,
                    $scope,
                    $stateParams,
                    $state) {

    const vm = this;

    const entity = {
        id: $stateParams.entityId,
        kind: $stateParams.kind
    };

    const perspectiveCode = $stateParams.perspectiveCode;


    const promises = [
        appStore.getById(entity.id),
        appCapabilityStore.findCapabilitiesByApplicationId(entity.id),
        perspectiveStore.findByCode(perspectiveCode),
        ratingStore.findByParent(entity.kind, entity.id),
        capabilityStore.findByAppIds([entity.id])
    ];


    const recalc = () => {
        vm.group.summaries = calculateGroupSummary(vm.group.raw);
        vm.changedRatings = determineChanges(vm.group);
    };


    $q.all(promises).then(
        ([app, appCapabilities, perspective, ratings, capabilities]) => {
            vm.app = app;
            const groupRef = {id: app.id, name: app.name, kind: 'APPLICATION'};
            vm.group = mkAppRatingsGroup(groupRef, perspective.measurables, capabilities, ratings);
            recalc();
        });


    const paintCell = (measureIdx, subjectIdx, rating = 'G') => {
        vm.group.raw[subjectIdx].ratings[measureIdx].current = rating;
        recalc();
    };


    const paintAllMeasures = (subjectIdx, rating = 'G') => {
        const subject = vm.group.raw[subjectIdx];
        _.each(subject.ratings, (r) => r.current = rating);
        recalc();
    };

    const paintAllSubjects = (measureIdx, rating = 'G') => {
        _.each(vm.group.raw, (r) => r.ratings[measureIdx].current = rating);
        recalc();
    };


    const handleCellPaintRequest = (measureIdx, subjectIdx, ctrlKey, shiftKey) => {

        const justCell = () => paintCell(measureIdx, subjectIdx, vm.selectedRating);
        const allMeasures = () => paintAllMeasures(subjectIdx, vm.selectedRating);
        const allSubjects = () => paintAllSubjects(measureIdx, vm.selectedRating);

        let paintFn = justCell;
        if (ctrlKey) paintFn = allMeasures;
        if (shiftKey) paintFn = allSubjects;

        if (ctrlKey && shiftKey) {
            paintFn = () => {
                allMeasures();
                allSubjects();
            };
        }


        $scope.$apply(paintFn);
    };


    const revert = (measureIdx, subjectIdx) => {
        const rating = vm.group.raw[subjectIdx].ratings[measureIdx];
        rating.current = rating.original;
        recalc();
    };


    const canPaint = () => d3.event.buttons && vm.selectedRating !== null;

    vm.tweakers = {
        subjectLabel: {
            enter: selection => selection.on('click.go', (d, i) => $scope.$apply(paintAllMeasures(i, vm.selectedRating)))
        },

        currentCell: {
            enter: selection =>
                selection
                    .on('mousedown.paint', (d, i, j) => {
                        if (canPaint()) handleCellPaintRequest(i, j, d3.event.ctrlKey, d3.event.shiftKey);
                    })
                    .on('mouseenter.paint', (d, i, j) => {
                        if (canPaint()) {
                            handleCellPaintRequest(i, j, d3.event.ctrlKey, d3.event.shiftKey);
                        }
                    })
        },

        originalCell: {
            enter: selection => selection.on('mousedown.revert', (d, i, j) => $scope.$apply(revert(i, j)))
        }
    };

    vm.selectedRating = 'G';

    vm.revertAll = () => {
        const revertRow = row => _.each(row.ratings, r => r.current = r.original);
        _.each(vm.group.raw, revertRow);
        notification('Reverted all changes');
        recalc();
    };

    vm.selectRating = (r) => {
        vm.selectedRating = r;
    };

    vm.submit = () => {
        const action = createChangeAction(vm.group, vm.app, perspectiveCode);
        ratingStore.update(action)
            .then(() => $state.go('main.app.view', { id: vm.app.id }))
            .then(() => notification.success('Updated ratings'));
    };

}

controller.$inject = [
    'ApplicationStore',
    'AppCapabilityStore',
    'PerspectiveStore',
    'RatingStore',
    'CapabilityStore',
    'Notification',
    '$q',
    '$scope',
    '$stateParams',
    '$state'
];


export default {
    template: require('./ratings-editor.html'),
    controller,
    controllerAs: 'ctrl'
};

