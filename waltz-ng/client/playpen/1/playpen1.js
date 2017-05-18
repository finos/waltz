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


const initData = {
    id: 3035,
};


function controller(
    $q,
    measurableCategoryStore,
    measurableRelationshipStore,
    measurableStore)
{
    const vm = Object.assign(this, initData);

    const promises = [
        measurableCategoryStore.findAll(),
        measurableRelationshipStore.findForMeasurable(vm.id),
        measurableStore.findAll()
    ];


    $q.all(promises)
        .then(([categories, relationships, measurables]) => {
            vm.categories = categories;
            vm.relationships = relationships;
            vm.measurables = measurables;
            vm.measurable = _.find(measurables, { id: vm.id });
        });

    const reloadRelationships = () => {
        measurableRelationshipStore
            .findForMeasurable(vm.id)
            .then(rs => vm.relationships = rs);
    };

    vm.saveRelationship = d => {
        const savePromise = measurableRelationshipStore.save(d);
        savePromise.then(reloadRelationships);
        return savePromise;
    };

    vm.removeRelationship = d => {
        const removePromise = measurableRelationshipStore.remove(d.measurableA, d.measurableB);
        removePromise.then(reloadRelationships)
        return removePromise;
    };
}


controller.$inject = [
    '$q',
    'MeasurableCategoryStore',
    'MeasurableRelationshipStore',
    'MeasurableStore'
];


const view = {
    template: require('./playpen1.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;