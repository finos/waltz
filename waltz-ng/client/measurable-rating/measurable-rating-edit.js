
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import _ from 'lodash';
import {initialiseData, kindToViewState} from '../common';
import {measurableKindNames} from  '../common/services/display-names';
import {measurableKindDescriptions} from  '../common/services/descriptions';

const BASE_EDITOR = {
    canSave: false,
    canRemove: false,
    rating: null,
    original: null,
    measurable: null
};


const initialState = {
    editor: null
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


function controller($q,
                    $state,
                    $stateParams,
                    applicationStore,
                    measurableStore,
                    measurableRatingStore) {

    const entityRef = {
        kind: $stateParams.kind,
        id: $stateParams.id
    };
    const vm = initialiseData(this, initialState);


    const measurablesPromise = measurableStore
        .findAll()
        .then(ms => vm.measurables = ms);

    const ratingsPromise = measurableRatingStore
        .findForEntityReference(entityRef)
        .then(rs => vm.ratings = rs);

    applicationStore
        .getById(entityRef.id)
        .then(app => vm.entityRef = Object.assign(
            entityRef,
            { name: app.name, description: app.description }));

    $q.all([ratingsPromise, measurablesPromise])
        .then(() => vm.tabs = prepareTabs(vm.ratings, vm.measurables));

    vm.backUrl = $state.href(
        kindToViewState(entityRef.kind),
        { id: entityRef.id });

    vm.onMeasurableSelect = (d) => {
        const original = d.rating
            ? { rating: d.rating.rating, description: d.rating.description }
            : {};

        vm.editor = Object.assign(
            {},
            BASE_EDITOR,
            {
                original,
                working:Object.assign({}, original),
                canRemove: ! _.isEmpty(original),
                measurable: d.measurable
            });
    };

    vm.onRatingSelect = r => {
        vm.editor.working.rating = r;
        vm.editor.canSave = calcCanSave(vm.editor.working, vm.editor.original);
    };

    vm.onCommentChange = () => {
        vm.editor.canSave = calcCanSave(vm.editor.working, vm.editor.original);
    };

    vm.doCancel = () => vm.editor = null;

    vm.doSave = () => {
        const saveParams = [entityRef, vm.editor.measurable.id, vm.editor.working.rating, vm.editor.working.description];
        const savePromise = _.isEmpty(vm.editor.original)
            ? measurableRatingStore.create(...saveParams)
            : measurableRatingStore.update(...saveParams);

        savePromise
            .then(rs => vm.ratings = rs)
            .then(() => vm.tabs = prepareTabs(vm.ratings, vm.measurables));

    };

    vm.doRemove = () => {
        measurableRatingStore
            .remove(entityRef, vm.editor.measurable.id)
            .then(rs => vm.ratings = rs)
            .then(() => vm.tabs = prepareTabs(vm.ratings, vm.measurables));
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
