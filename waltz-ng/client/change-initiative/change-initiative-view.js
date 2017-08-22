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
import * as _ from "lodash";
import {CORE_API} from '../common/services/core-api-utils';
import {groupRelationships, enrichRelationships} from "./change-initiative-utils";
import template from './change-initiative-view.html';


const initialState = {
    changeInitiative: {},
    involvements: [],
    sourceDataRatings: [],
    surveyInstances: [],
    surveyRuns: [],
    related: {
        appGroupRelationships: []
    }
};


function controller($q,
                    $stateParams,
                    applicationStore,
                    appGroupStore,
                    historyStore,
                    involvedSectionService,
                    serviceBroker,
                    sourceDataRatingStore,
                    surveyInstanceStore,
                    surveyRunStore) {

    const {id} = $stateParams;
    const vm = Object.assign(this, initialState);

    vm.entityRef = {
        kind: 'CHANGE_INITIATIVE',
        id: id
    };

    const loadRelatedEntities = (id, rels = []) => {

        const relationships = groupRelationships(id, rels);

        const appGroupIds = _.map(relationships.APP_GROUP, 'entity.id');

        const promises = [
            appGroupStore.findByIds(appGroupIds),
            applicationStore.findBySelector({
                entityReference: vm.entityRef,
                scope: 'EXACT'
            })
        ];

        return $q
            .all(promises)
            .then(([appGroups, apps]) => {
                const appGroupRelationships = enrichRelationships(relationships.APP_GROUP, appGroups);
                const appRelationships = enrichRelationships(relationships.APPLICATION, apps);

                return {appGroupRelationships, appRelationships}
            });

    };

    serviceBroker
        .loadViewData(CORE_API.ChangeInitiativeStore.getById, [id])
        .then(result => {
            const ci = result.data;
            vm.changeInitiative = ci;
            vm.entityRef = Object.assign({},
                                         vm.entityRef,
                                         {
                                            name: vm.changeInitiative.name,
                                            description: vm.changeInitiative.description
                                         });

            historyStore
                .put(
                    ci.name,
                    'CHANGE_INITIATIVE',
                    'main.change-initiative.view',
                    { id: ci.id });
        });

    sourceDataRatingStore
        .findAll()
        .then(rs => vm.sourceDataRatings = rs);

    serviceBroker
        .loadViewData(CORE_API.ChangeInitiativeStore.findRelatedForId, [id])
        .then(result => loadRelatedEntities(id, result.data))
        .then(related => vm.related = related);

    surveyRunStore
        .findByEntityReference(vm.entityRef)
        .then(surveyRuns => vm.surveyRuns = surveyRuns);

    // only get back completed instances
    surveyInstanceStore
        .findByEntityReference(vm.entityRef)
        .then(surveyInstances => vm.surveyInstances = _.filter(surveyInstances, {'status': 'COMPLETED'}));

}


controller.$inject = [
    '$q',
    '$stateParams',
    'ApplicationStore',
    'AppGroupStore',
    'HistoryStore',
    'InvolvedSectionService',
    'ServiceBroker',
    'SourceDataRatingStore',
    'SurveyInstanceStore',
    'SurveyRunStore'
];


const page = {
    template,
    controller,
    controllerAs: 'ctrl'
};


export default page;

