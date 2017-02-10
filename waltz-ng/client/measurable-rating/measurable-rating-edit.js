
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
import {initialiseData} from '../common';
import {kindToViewState} from '../common/link-utils';


const initialState = {
    backUrl: null,
    selected: null,
    entityRef: null,
    measurables: [],
    categories: [],
    ratings: [],
    tabs: [],
    saveInProgress: false,
    visibility: {
        tab: null
    }
};


function prepareTabs(categories = [], measurables = [], ratings = []) {
    const measurablesByCategory = _.groupBy(measurables, 'categoryId');

    const tabs = _.map(categories, category => {
        const measurablesForCategory = measurablesByCategory[category.id];
        const ids = _.map(measurablesForCategory, 'id');
        const ratingsForCategory = _.filter(ratings, r => _.includes(ids, r.measurableId))
        return {
            category,
            measurables: measurablesForCategory,
            ratings: ratingsForCategory
        };
    });

    return _.sortBy(
        tabs,
        g => g.category.name);
}


function determineSelectedTab(tabs = []) {
    // first with ratings, or simply first if no ratings
    const tab = _.find(tabs, t => t.ratings.length > 0 ) || tabs[0];
    return _.get(tab, 'category.id');
}


function determineSaveFn(selected, store) {
    return _.isEmpty(selected.rating)
        ? store.create
        : store.update;
}


function controller($q,
                    $state,
                    $stateParams,
                    applicationStore,
                    measurableStore,
                    measurableCategoryStore,
                    measurableRatingStore,
                    notification,
                    perspectiveDefinitionStore) {

    const entityReference = {
        kind: $stateParams.kind,
        id: $stateParams.id
    };

    const vm = initialiseData(this, initialState);

    // -- LOAD ---

    const categoryPromise = measurableCategoryStore
        .findAll()
        .then(cs => vm.categories = cs);

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

    $q.all([ratingsPromise, measurablesPromise, categoryPromise])
        .then(() => {
            vm.tabs = prepareTabs(vm.categories, vm.measurables, vm.ratings);
            vm.visibility.tab = determineSelectedTab(vm.tabs);
        });

    perspectiveDefinitionStore
        .findAll()
        .then(pds => vm.perspectiveDefinitions = pds);


    // -- INTERACT ---

    const getDescription = () => vm.selected.rating
        ? vm.selected.rating.description
        : null;

    const getRating = () => vm.selected.rating
        ? vm.selected.rating.rating
        : null;

    const doSave = (rating, description) => {
        const saveFn = determineSaveFn(vm.selected, measurableRatingStore);

        const savePromise = saveFn(
            entityReference,
            vm.selected.measurable.id,
            rating,
            description);

        return savePromise
            .then(rs => vm.ratings = rs)
            .then(() => vm.tabs = prepareTabs(vm.categories, vm.measurables, vm.ratings))
            .then(() => {
                vm.saveInProgress = false;
                const newRating = { rating, description };
                vm.selected = Object.assign({}, vm.selected, { rating: newRating });
            });
    };

    const doRemove = () => {

        if (! vm.selected.rating) return $q.reject();

        vm.saveInProgress = true;

        return measurableRatingStore
            .remove(entityReference, vm.selected.measurable.id)
            .then(rs => {
                vm.saveInProgress = false;
                vm.ratings = rs;
                vm.tabs = prepareTabs(vm.categories, vm.measurables, vm.ratings);
                vm.selected.rating = null;
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
        const category = _.find(vm.categories, ({ id: measurable.categoryId }));
        vm.selected = { rating, measurable, category };
    };

    vm.onRatingSelect = r => {
        if (! vm.selected.measurable) return; // nothing selected
        if (r === getRating()) return; // rating not changed

        if (r === 'X') {
            return doRemove()
                .then(() => notification.success('Removed'));
        } else {
            return doSave(r, getDescription())
                .then(() => notification.success('Saved'));
        }
    };

    vm.onSaveComment = (comment) => {
        return doSave(getRating(), comment)
            .then(() => notification.success('Saved Comment'))
    };

    vm.doCancel = reset;

    vm.onTabChange = (categoryId) => {
        reset();

    };

    vm.onKeypress = (evt) => {
        const goRed = () => vm.onRatingSelect('R');
        const goGreen = () => vm.onRatingSelect('G');
        const goAmber = () => vm.onRatingSelect('A');
        const remove = () => vm.onRatingSelect('X');
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
    'MeasurableCategoryStore',
    'MeasurableRatingStore',
    'Notification',
    'PerspectiveDefinitionStore'
];


export default {
    template: require('./measurable-rating-edit.html'),
    controller,
    controllerAs: 'ctrl'
};
