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
import {groupRelationships, enrichRelationships} from "./change-initiative-utils";
import {aggregatePeopleInvolvements} from "../involvement/involvement-utils";
import * as _ from "lodash";

const initialState = {
    bookmarks: [],
    changeInitiative: {},
    involvements: [],
    namedNotes: [],
    namedNoteTypes: [],
    sourceDataRatings: [],
    surveyInstances: [],
    surveyRuns: [],
    related: {
        appGroups: []
    }
};


function controller($q,
                    $stateParams,
                    applicationStore,
                    appGroupStore,
                    bookmarkStore,
                    changeInitiativeStore,
                    entityNamedNoteStore,
                    entityNamedNoteTypeService,
                    historyStore,
                    involvementStore,
                    notification,
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


    changeInitiativeStore
        .getById(id)
        .then(ci => {
            vm.changeInitiative = ci;
            historyStore
                .put(
                    ci.name,
                    'CHANGE_INITIATIVE',
                    'main.change-initiative.view',
                    { id: ci.id });
        });


    const loadNamedNotes =
        () => entityNamedNoteStore
            .findByEntityReference(vm.entityRef)
            .then(notes => vm.namedNotes = notes);

    loadNamedNotes();

    entityNamedNoteTypeService
        .loadNoteTypes()
        .then(noteTypes => vm.namedNoteTypes = noteTypes);

    sourceDataRatingStore
        .findAll()
        .then(rs => vm.sourceDataRatings = rs);


    bookmarkStore
        .findByParent({kind: 'CHANGE_INITIATIVE', id})
        .then(bs => vm.bookmarks = bs);


    changeInitiativeStore
        .findRelatedForId(id)
        .then(rels => loadRelatedEntities(id, rels))
        .then(related => vm.related = related);

    surveyRunStore
        .findByEntityReference(vm.entityRef)
        .then(surveyRuns => vm.surveyRuns = surveyRuns);

    // only get back completed instances
    surveyInstanceStore
        .findByEntityReference(vm.entityRef)
        .then(surveyInstances => vm.surveyInstances = _.filter(surveyInstances, {'status': 'COMPLETED'}));

    const involvementPromises = [
        involvementStore.findByEntityReference('CHANGE_INITIATIVE', id),
        involvementStore.findPeopleByEntityReference('CHANGE_INITIATIVE', id)
    ];
    $q.all(involvementPromises)
        .then(([relations, people]) => aggregatePeopleInvolvements(relations, people))
        .then(involvements => vm.involvements = involvements);


    vm.saveNamedNote = (note) => {
        entityNamedNoteStore
            .save(vm.entityRef, note.namedNoteTypeId, {newStringVal: note.noteText })
            .then(rc => {
                if (rc) {
                    notification.success('Note saved successfully');
                    loadNamedNotes();
                } else {
                    notification.error('Failed to save note');
                }
            })
    };

    vm.deleteNamedNote = (note) => {
        entityNamedNoteStore
            .remove(vm.entityRef, note.namedNoteTypeId)
            .then(rc => {
                if (rc) {
                    notification.success('Note deleted successfully');
                    loadNamedNotes();
                } else {
                    notification.error('Failed to delete note');
                }
            })
    }
}


controller.$inject = [
    '$q',
    '$stateParams',
    'ApplicationStore',
    'AppGroupStore',
    'BookmarkStore',
    'ChangeInitiativeStore',
    'EntityNamedNoteStore',
    'EntityNamedNoteTypeService',
    'HistoryStore',
    'InvolvementStore',
    'Notification',
    'SourceDataRatingStore',
    'SurveyInstanceStore',
    'SurveyRunStore'
];


const page = {
    template: require('./change-initiative-view.html'),
    controller,
    controllerAs: 'ctrl'
};


export default page;

