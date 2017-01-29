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


const initialState = {
    tabs: [],
    visibility: {
        tab: null
    },
};


function prepareTabs(categories = [], measurables = [], counts = []) {
    const countsById = _.keyBy(counts, 'id');

    const measurablesByCategory = _.chain(measurables)
        .map(m => {
            const directCount = (countsById[m.id] || { count: 0 }).count;
            return Object.assign({}, m, { directCount })
        })
        .groupBy('categoryId')
        .value();

    const tabs = _.map(categories, c => {
        return {
            category: c,
            measurables: measurablesByCategory[c.id]
        };
    });

    return _.sortBy(
        tabs,
        g => g.category.name);
}


function findFirstNonEmptyTab(tabs = []) {
    const tab = _.find(tabs, t => (t.measurables || []).length > 0);
    return _.get(tab || tabs[0], 'category.id');
}


function controller($q,
                    $state,
                    $stateParams,
                    measurableStore,
                    measurableCategoryStore,
                    measurableRatingStore,
                    staticPanelStore,
                    svgStore) {

    const vm = initialiseData(this, initialState);

    const measurablePromise = measurableStore
        .findAll();

    const measurableCategoryPromise = measurableCategoryStore
        .findAll()
        .then(cs => vm.categories = cs);

    const countPromise = measurableRatingStore
        .countByMeasurable();

    $q.all([measurablePromise, measurableCategoryPromise, countPromise])
        .then(([measurables = [], categories = [], counts = []]) => {
            vm.tabs = prepareTabs(categories, measurables, counts);
            vm.measurablesByExternalId = _.keyBy(measurables, 'externalId');
            vm.visibility.tab = $stateParams.category || findFirstNonEmptyTab(vm.tabs);
        });

    measurableCategoryPromise
        .then((cs) => staticPanelStore.findByGroups(_.map(cs, c => `HOME.MEASURABLE.${c.id}`)))
        .then(panels => vm.panelsByCategory = _.groupBy(panels, d => d.group.replace('HOME.MEASURABLE.', '')));

    measurableCategoryPromise
        .then((cs) => svgStore.findByGroups(_.map(cs, c => `NAVAID.MEASURABLE.${c.id}`)))
        .then(diagrams => vm.diagramsByCategory = _.groupBy(diagrams, d => d.group.replace('NAVAID.MEASURABLE.', '')));

    measurableCategoryPromise
        .then(cs => vm.categoriesById = _.keyBy(cs, 'id'));

    vm.blockProcessor = b => {
        const extId = b.value;
        const measurable = vm.measurablesByExternalId[extId];
        if (measurable) {
            b.block.onclick = () => $state.go('main.measurable.view', { id: measurable.id });
            angular.element(b.block).addClass('clickable');
        } else {
            console.log(`MeasurableList: Could not find measurable with external id: ${extId}`, b)
        }
    };

}


controller.$inject = [
    '$q',
    '$state',
    '$stateParams',
    'MeasurableStore',
    'MeasurableCategoryStore',
    'MeasurableRatingStore',
    'StaticPanelStore',
    'SvgDiagramStore'
];


export default {
    template: require('./measurable-list.html'),
    controller,
    controllerAs: 'ctrl'
};
