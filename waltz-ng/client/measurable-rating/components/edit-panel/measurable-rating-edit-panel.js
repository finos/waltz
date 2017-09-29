/*
 *
 *  * Waltz - Enterprise Architecture
 *  * Copyright (C) 2017  Khartec Ltd.
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Lesser General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/*
 *
 *  * Waltz - Enterprise Architecture
 *  * Copyright (C) 2017  Khartec Ltd.
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Lesser General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import _ from 'lodash';
import { CORE_API } from '../../../common/services/core-api-utils';
import { initialiseData } from '../../../common';
import { kindToViewState } from '../../../common/link-utils';

import template from './measurable-rating-edit-panel.html';


const bindings = {
    parentEntityRef: '<'
};


function prepareTabs(categories = [], measurables = [], ratings = []) {
    const measurablesByCategory = _.groupBy(measurables, 'categoryId');

    const tabs = _.map(categories, category => {
        const measurablesForCategory = measurablesByCategory[category.id];
        const ids = _.map(measurablesForCategory, 'id');
        const ratingsForCategory = _.filter(ratings, r => _.includes(ids, r.measurableId));
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


const initialState = {
    selected: null,
    measurables: [],
    categories: [],
    ratings: [],
    tabs: [],
    saveInProgress: false,
    visibility: {
        tab: null
    }
};


function controller($q,
                    $state,
                    measurableRatingStore,
                    notification,
                    serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        loadData(true);
    };


    const loadData = (force) => {
        // -- LOAD ---

        const categoryPromise = serviceBroker
            .loadViewData(CORE_API.MeasurableCategoryStore.findAll, [], { force })
            .then(r => vm.categories = r.data);

        const measurablesPromise = serviceBroker
            .loadViewData(CORE_API.MeasurableStore.findAll, [], { force })
            .then(r => vm.measurables = r.data);

        const ratingsPromise = serviceBroker
            .loadViewData(CORE_API.MeasurableRatingStore.findByAppSelector, [{ entityReference: vm.parentEntityRef, scope: 'EXACT' }], { force })
            .then(r => vm.ratings = r.data);

        serviceBroker
            .loadViewData(CORE_API.ApplicationStore.getById, [vm.parentEntityRef.id], { force })
            .then(r => vm.entityRating = r.data.overallRating);


        $q.all([ratingsPromise, measurablesPromise, categoryPromise])
            .then(() => {
                vm.tabs = prepareTabs(vm.categories, vm.measurables, vm.ratings);
                vm.visibility.tab = determineSelectedTab(vm.tabs);
            });

        serviceBroker
            .loadViewData(CORE_API.PerspectiveDefinitionStore.findAll, [], { force })
            .then(r => vm.perspectiveDefinitions = r.data);
    };

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
            vm.parentEntityRef,
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
            .remove(vm.parentEntityRef, vm.selected.measurable.id)
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
            kindToViewState(vm.parentEntityRef.kind),
            { id: vm.parentEntityRef.id });

    vm.onMeasurableSelect = (measurable, rating) => {
        const category = _.find(vm.categories, ({ id: measurable.categoryId }));
        vm.selected = { rating, measurable, category };
    };

    vm.onRatingSelect = r => {
        if (! vm.selected.measurable) return; // nothing selected
        if (! vm.selected.measurable.concrete) return; // not concrete
        if (r === getRating()) return; // rating not changed

        return r === 'X'
            ? doRemove()
                .then(() => notification.success('Removed'))
            : doSave(r, getDescription())
                .then(() => notification.success('Saved'));
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
    'MeasurableRatingStore',
    'Notification',
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzMeasurableRatingEditPanel'
};
