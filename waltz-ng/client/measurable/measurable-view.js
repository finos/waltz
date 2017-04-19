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
import {getParents, populateParents} from '../common/hierarchy-utils';
import {aggregatePeopleInvolvements} from '../involvement/involvement-utils';


const initialState = {

};


function logHistory(measurable, historyStore) {
    return historyStore
        .put(measurable.name,
            'MEASURABLE',
            'main.measurable.view',
            { id: measurable.id });
}


function controller($q,
                    $scope,
                    $stateParams,
                    applicationStore,
                    assetCostViewService,
                    bookmarkStore,
                    changeLogStore,
                    complexityStore,
                    entityStatisticStore,
                    historyStore,
                    involvementStore,
                    logicalFlowViewService,
                    measurableStore,
                    measurableCategoryStore,
                    measurableRatingStore,
                    sourceDataRatingStore,
                    technologyStatsService) {

    const id = $stateParams.id;
    const ref = { id, kind: 'MEASURABLE' };
    const childrenSelector = { entityReference: ref, scope: 'CHILDREN' }

    const vm = initialiseData(this, initialState);
    vm.entityReference = ref;


    // -- LOAD ---

    const loadWave1 = () =>
        $q.all([
            measurableStore
                .findAll()
                .then(all => {
                    vm.allMeasurables = all;
                    const withParents = populateParents(all);
                    vm.measurable = _.find(withParents, { id });
                    vm.parents = getParents(vm.measurable);
                    vm.children = _.chain(all)
                        .filter({ parentId: id })
                        .sortBy('name')
                        .value();
                }),
        ]);

    const loadWave2 = () =>
        $q.all([
            measurableCategoryStore
                .findAll()
                .then(cs => {
                    vm.measurableCategories = cs;
                    vm.measurableCategory = _.find(cs, { id: vm.measurable.categoryId });
                }),
            measurableRatingStore
                .findByMeasurableSelector(childrenSelector)
                .then(rs => vm.ratings = rs),
            measurableRatingStore
                .statsForRelatedMeasurables(id)
                .then(rs => vm.relatedStats = rs),
            applicationStore
                .findBySelector(childrenSelector)
                .then(apps => vm.applications = apps),
            logicalFlowViewService
                .initialise(childrenSelector)
                .then(flowView => vm.logicalFlowView = flowView),
            bookmarkStore
                .findByParent(ref)
                .then(bookmarks => vm.bookmarks = bookmarks),
            $q.all([
                involvementStore.findByEntityReference('MEASURABLE', id),
                involvementStore.findPeopleByEntityReference('MEASURABLE', id)
            ]).then(([involvements, people]) =>
                vm.peopleInvolvements = aggregatePeopleInvolvements(involvements, people)
            )
        ]);

    const loadWave3 = () =>
        $q.all([
            entityStatisticStore
                .findAllActiveDefinitions()
                .then(statDefinitions => vm.entityStatisticDefinitions = statDefinitions),
            complexityStore
                .findBySelector(childrenSelector)
                .then(complexity => vm.complexity = complexity),
            assetCostViewService
                .initialise(childrenSelector, 2016)
                .then(costs => vm.assetCostData = costs),
            technologyStatsService
                .findBySelector(childrenSelector)
                .then(techStats => vm.techStats = techStats)
        ]);

    const loadWave4 = () =>
        $q.all([
            sourceDataRatingStore
                .findAll()
                .then(sourceDataRatings => vm.sourceDataRatings = sourceDataRatings),
            changeLogStore
                .findByEntityReference(ref)
                .then(changeLogs => vm.changeLogs = changeLogs),
            logHistory(vm.measurable, historyStore)
        ]);


    // -- BOOT ---

    loadWave1()
        .then(loadWave2)
        .then(loadWave3)
        .then(loadWave4);


    // -- INTERACTION ---

    vm.loadAllCosts = () => $scope
        .$applyAsync(() => {
            assetCostViewService
                .loadDetail()
                .then(data => vm.assetCostData = data);
        });

    vm.loadFlowDetail = () => logicalFlowViewService
        .loadDetail()
        .then(flowView => vm.logicalFlowView = flowView);

}


controller.$inject = [
    '$q',
    '$scope',
    '$stateParams',
    'ApplicationStore',
    'AssetCostViewService',
    'BookmarkStore',
    'ChangeLogStore',
    'ComplexityStore',
    'EntityStatisticStore',
    'HistoryStore',
    'InvolvementStore',
    'LogicalFlowViewService',
    'MeasurableStore',
    'MeasurableCategoryStore',
    'MeasurableRatingStore',
    'SourceDataRatingStore',
    'TechnologyStatisticsService'
];


export default {
    template: require('./measurable-view.html'),
    controller,
    controllerAs: 'ctrl'
};
