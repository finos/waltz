
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
import _ from 'lodash';
import {initialiseData, kindToViewState} from '../common';
import {measurableKindNames} from  '../common/services/display-names';
import {measurableKindDescriptions} from  '../common/services/descriptions';


const initialState = {
    backUrl: null,
    selected: null,
    entityRef: null,
    measurables: [],
    ratings: [],
    tabs: [],
    saveInProgress: false,
    visibility: {
        tab: null
    }
};


function prepareTabs(ratings = [], measurables = []) {
    const allMeasurableKinds = _.keys(measurableKindNames);
    const measurablesByKind = _.groupBy(measurables, 'kind');


    const tabs = _.map(allMeasurableKinds, k => {
        const measurablesForKind = measurablesByKind[k];
        const ids = _.map(measurablesForKind, 'id');
        const ratingsForKind = _.filter(ratings, r => _.includes(ids, r.measurableId))
        return {
            kind: {
                code: k,
                name: measurableKindNames[k],
                description: measurableKindDescriptions[k]
            },
            measurables: measurablesForKind,
            ratings: ratingsForKind
        };
    });

    return _.sortBy(
        tabs,
        g => g.kind.name);
}


function determineSelectedTab(tabs = []) {
    // first with ratings, or simply first if no ratings
    const tab = _.find(tabs, t => t.ratings.length > 0 ) || tabs[0];
    return _.get(tab, 'kind.code');
}


function controller($q,
                    $state,
                    $stateParams,
                    applicationStore,
                    measurableStore,
                    measurableRatingStore,
                    notification) {

    const entityReference = {
        kind: $stateParams.kind,
        id: $stateParams.id
    };

    const vm = initialiseData(this, initialState);

    // -- LOAD ---

    const measurablesPromise = measurableStore
        .findAll()
        .then(ms => vm.measurables = ms);

    const ratingsPromise = measurableRatingStore
        .findByAppSelector( { entityReference, scope: 'EXACT' })
        .then(rs => vm.ratings = rs);

    applicationStore
        .getById(entityReference.id)
        .then(app => {
            vm.entityRating = app.overallRating;
            vm.entityRef = Object.assign(
                entityReference,
                { name: app.name, description: app.description })
        });

    $q.all([ratingsPromise, measurablesPromise])
        .then(() => {
            vm.tabs = prepareTabs(vm.ratings, vm.measurables);
            vm.visibility.tab = determineSelectedTab(vm.tabs);
        });


    // -- INTERACT ---

    const getDescription = () => vm.selected.rating
        ? vm.selected.rating.description
        : null;

    const getRating = () => vm.selected.rating
        ? vm.selected.rating.rating
        : null;

    const doSave = (rating, description) => {
        const saveFn = _.isEmpty(vm.selected.rating)
            ? measurableRatingStore.create
            : measurableRatingStore.update;

        const savePromise = saveFn(
            entityReference,
            vm.selected.measurable.id,
            rating,
            description);

        return savePromise
            .then(rs => vm.ratings = rs)
            .then(() => vm.tabs = prepareTabs(vm.ratings, vm.measurables))
            .then(() => {
                vm.saveInProgress = false;
                vm.selected = {
                    rating: { rating, description },
                    measurable: vm.selected.measurable
                };
            });
    };

    const reset = () => {
        vm.saveInProgress = false;
        vm.selected = {};
    };

    vm.backUrl = $state
        .href(
            kindToViewState(entityReference.kind),
            { id: entityReference.id });

    vm.onMeasurableSelect = (measurable, rating) => {
        vm.selected = { rating, measurable };
    };

    vm.onRatingSelect = r => {
        if (! vm.selected.measurable) return; // nothing selected
        if (r === getRating()) return; // rating not changed

        return doSave(r, getDescription())
            .then(() => notification.success('Saved'));
    };

    vm.onSaveComment = (comment) => {
        return doSave(getRating(), comment)
            .then(() => notification.success('Saved Comment'))
    };

    vm.doCancel = reset;

    vm.doRemove = () => {
        if (! vm.selected.rating) return;

        vm.saveInProgress = true;

        measurableRatingStore
            .remove(entityReference, vm.selected.measurable.id)
            .then(rs => {
                vm.saveInProgress = false;
                vm.ratings = rs;
                vm.tabs = prepareTabs(vm.ratings, vm.measurables);
                vm.selected.rating = null;
                notification.success('Removed');
            });
    };


    vm.onKeypress = (evt) => {
        const goRed = () => vm.onRatingSelect('R');
        const goGreen = () => vm.onRatingSelect('G');
        const goAmber = () => vm.onRatingSelect('A');
        const remove = () => vm.doRemove();
        const cancel = () => vm.doCancel();

        const keyActions = {
            'r': goRed,
            'R': goRed,
            'a': goAmber,
            'A': goAmber,
            'y': goAmber,
            'Y': goAmber,
            'g': goGreen,
            'G': goGreen,
            'x': remove,
            'X': remove,
            27: cancel,
        };

        const action = keyActions[evt.keyCode] || keyActions[evt.key];

        if (action) action();
    };
}


controller.$inject = [
    '$q',
    '$state',
    '$stateParams',
    'ApplicationStore',
    'MeasurableStore',
    'MeasurableRatingStore',
    'Notification'
];


export default {
    template: require('./measurable-rating-edit.html'),
    controller,
    controllerAs: 'ctrl'
};
