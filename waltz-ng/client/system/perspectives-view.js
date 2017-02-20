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
import {baseRagNames} from '../ratings/rating-utils';


const initialState = {
    model: {},
    form: {},
    visibility: {
        editor: false
    }
};


function controller(notification,
                    measurableCategoryStore,
                    perspectiveDefinitionStore) {

    const vm = initialiseData(this, initialState);

    measurableCategoryStore
        .findAll()
        .then(cs => vm.categories = _.sortBy(cs, 'name'));

    perspectiveDefinitionStore
        .findAll()
        .then(ps => vm.perspectives = ps);

    vm.select = (p) => {
        vm.selectedPerspective = p;
        vm.onCancelForm();
    };

    vm.deselect = (p) => vm.selectedPerspective = null;

    vm.onStartCreate = () => {
        vm.visibility = Object.assign({}, vm.visibility, { editor: true });
        const ragNames = Object.assign({}, baseRagNames)
        vm.model = Object.assign({}, { ragNames })
    };

    vm.onCancelForm = () => {
        vm.visibility = Object.assign({}, vm.visibility, { editor: false });
    };

    vm.validateCombo = (cx, cy) => {
        const allExisting =_.map(vm.perspectives, p => [p.categoryX, p.categoryY]);
        const exists = _.some(allExisting, pair => _.isEqual(pair, [cx, cy]) || _.isEqual(pair, [cy, cx]));
        const same = cx === cy;
        const free = !exists && !same;

        vm.form.categoryX.$invalid = !free;
        vm.model.free = free
            ? 'y'
            : null;
    };

    vm.onSubmitForm = () => {
        const perspectiveDefinition = _.omit(vm.model, 'free');

        perspectiveDefinitionStore
            .create(perspectiveDefinition)
            .then(ps => vm.perspectives = ps)
            .then(() => {
                notification.success("Perspective created");
                vm.onCancelForm();
            });
    };

}

controller.$inject = [
    'Notification',
    'MeasurableCategoryStore',
    'PerspectiveDefinitionStore'
];


const page = {
    controller,
    controllerAs: 'ctrl',
    template: require('./perspectives-view.html')
};


export default page;