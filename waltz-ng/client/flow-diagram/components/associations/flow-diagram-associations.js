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

import template from './flow-diagram-associations.html';
import {initialiseData, termSearch} from '../../../common';
import {CORE_API} from '../../../common/services/core-api-utils';


const bindings = {
    diagramId: '<',
    readOnly: '<'
};


const initialState = {
    associations: [],
    visibility: {
        suggestions: false,
        search: false,
        inactive: false
    },
    readOnly: true
};


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);


    // -- BOOT --

    const loadMappings = () => {
        const associationsPromise = serviceBroker
            .loadViewData(
                CORE_API.FlowDiagramEntityStore.findByDiagramId,
                [vm.diagramId],
                { force: true })
            .then(r => {
                return _
                    .chain(r.data)
                    .map('entityReference')
                    .filter(ref => ref.kind === 'MEASURABLE')
                    .value();
            });

        const likelyMeasurablePromise = serviceBroker
            .loadViewData(
                CORE_API.MeasurableStore.findMeasurablesBySelector,
                [vm.diagramRef],
                { force: true })
            .then(r => {
                return r.data;
            });

        const categoryPromise = serviceBroker
            .loadAppData(
                CORE_API.MeasurableCategoryStore.findAll,
                [])
            .then(r => {
                return r.data;
            });

        $q.all([associationsPromise, likelyMeasurablePromise, categoryPromise])
            .then(([associations, likelyMeasurables, measurableCategories]) => {
                const associatedIds = _.map(associations, 'id');
                const likelyById = _.keyBy(likelyMeasurables, 'id');

                vm.associatedMeasurables = _
                    .chain(associatedIds)
                    .map(aId => likelyById[aId])
                    .sortBy('name')
                    .value();

                vm.suggestedMeasurables = _
                    .chain(likelyMeasurables)
                    .reject(m => _.includes(associatedIds, m.id)) // remove already used ones
                    .sortBy('name')
                    .value();

                vm.categories = _.sortBy(measurableCategories, 'name');
            });
    };


    vm.$onChanges = () => {
        if (vm.diagramId) {
            vm.diagramRef = {
                entityReference: {
                    kind: 'FLOW_DIAGRAM',
                    id: vm.diagramId
                }
                ,
                scope: 'EXACT'
            };
            loadMappings();
            vm.visibility.inactive = false;
        } else {
            vm.visibility.inactive = true;
        }
    };


    // -- INTERACT --

    function hideAll() {
        vm.visibility.search = false;
        vm.visibility.suggestions = false;
    }

    vm.onShowSearch = () => {
        hideAll();
        vm.visibility.search = true;
    };

    vm.onShowSuggestions = () => {
        hideAll();
        vm.visibility.suggestions = true;
    };

    vm.doSearch = (qry = null, category) => {
        if (_.size(qry) === 0) {
            vm.searchResults = [];
            return;
        }

        serviceBroker
            .loadAppData(
                CORE_API.MeasurableStore.findAll,
                [])
            .then(r => {
                const measurables = _.filter(
                    r.data,
                    m => category
                        ? m.categoryId === category.id
                        : true);

                vm.searchResults = termSearch(
                    measurables,
                    qry,
                    ['name']);
            });
    };


    // -- CRUD --

    vm.onRemove = (m) => {
        serviceBroker
            .execute(
                CORE_API.FlowDiagramEntityStore.removeMeasurable,
                [vm.diagramId, m.id])
            .then(() => loadMappings());
    };


    vm.onAdd = (m) => {
        hideAll();
        serviceBroker
            .execute(
                CORE_API.FlowDiagramEntityStore.addMeasurable,
                [vm.diagramId, m.id])
            .then(() => loadMappings());
    }
}


controller.$inject = [
    '$q',
    'ServiceBroker'
];


const id = 'waltzFlowDiagramAssociations';


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id
};