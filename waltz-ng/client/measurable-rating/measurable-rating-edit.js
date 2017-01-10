
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


const BASE_EDITOR = {
    canSave: false,
    canRemove: false,
    original: null,
    measurable: null,
    working: null
};


const initialState = {
    backUrl: null,
    defaultRating: null,
    editor: null,
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


function calcCanSave(working, original) {
    const ratingsDiffer = working.rating !== original.rating;
    const descriptionsDiffer = working.description !== original.description;
    return ratingsDiffer || descriptionsDiffer;
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
                    measurableRatingStore) {

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
            vm.defaultRating = app.overallRating;
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

    vm.backUrl = $state
        .href(
            kindToViewState(entityReference.kind),
            { id: entityReference.id });

    vm.onMeasurableSelect = (d) => {
        const original = d.rating
            ? { rating: d.rating.rating, description: d.rating.description }
            : { };

        vm.editor = Object.assign(
            {},
            BASE_EDITOR,
            {
                original,
                working: Object.assign({ rating: vm.defaultRating }, original),
                canRemove: ! _.isEmpty(original),
                canSave: _.isEmpty(original),
                measurable: d.measurable
            });
    };

    vm.onRatingSelect = r => {
        vm.editor.working.rating = r;
        vm.defaultRating = r;
        vm.editor.canSave = calcCanSave(vm.editor.working, vm.editor.original);
    };

    vm.onCommentChange = () => {
        vm.editor.canSave = calcCanSave(vm.editor.working, vm.editor.original);
    };

    vm.doCancel = () => vm.editor = null;

    vm.doSave = () => {
        const saveParams = [entityReference, vm.editor.measurable.id, vm.editor.working.rating, vm.editor.working.description];
        const savePromise = _.isEmpty(vm.editor.original)
            ? measurableRatingStore.create(...saveParams)
            : measurableRatingStore.update(...saveParams);

        savePromise
            .then(rs => vm.ratings = rs)
            .then(() => vm.tabs = prepareTabs(vm.ratings, vm.measurables))
            .then(() => {
                vm.saveInProgress = false;
                vm.editor = null;
            });

    };

    vm.doRemove = () => {
        vm.saveInProgress = true;
        measurableRatingStore
            .remove(entityReference, vm.editor.measurable.id)
            .then(rs => vm.ratings = rs)
            .then(() => vm.tabs = prepareTabs(vm.ratings, vm.measurables))
            .then(() => {
                vm.saveInProgress = false;
                vm.editor = null;
            });
    };
}


controller.$inject = [
    '$q',
    '$state',
    '$stateParams',
    'ApplicationStore',
    'MeasurableStore',
    'MeasurableRatingStore',
];


export default {
    template: require('./measurable-rating-edit.html'),
    controller,
    controllerAs: 'ctrl'
};
