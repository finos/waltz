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

import {aggregatePeopleInvolvements} from "../involvement/involvement-utils";

const initialState = {
    bookmarks: [],
    changeInitiative: {},
    involvements: [],
    sourceDataRatings: [],
    related: {
        appGroups: []
    }
};


function controller($q,
                    $stateParams,
                    appGroupStore,
                    bookmarkStore,
                    changeInitiativeStore,
                    involvementStore,
                    sourceDataRatingStore) {

    const { id } = $stateParams;
    const vm = Object.assign(this, initialState);

    const loadRelatedEntities = (id, rels = []) => {

        const relatedByKind = _.chain(rels)
            .flatMap(rel => ([rel.a, rel.b]))
            .reject(ref => ref.kind === 'CHANGE_INITIATIVE' && ref.id === id)
            .groupBy('kind', 'id')
            .mapValues(vs => _.map(vs, 'id'))
            .value();

        const promises = [
            appGroupStore.findByIds(relatedByKind.APP_GROUP)
        ];

        return $q
            .all(promises)
            .then(([appGroups]) => {
                return { appGroups }
            });

    };


    changeInitiativeStore
        .getById(id)
        .then(ci => vm.changeInitiative = ci);


    sourceDataRatingStore
        .findAll()
        .then(rs => vm.sourceDataRatings = rs);


    bookmarkStore
        .findByParent({ kind: 'CHANGE_INITIATIVE', id })
        .then(bs => vm.bookmarks = bs);


    changeInitiativeStore
        .findRelatedForId(id)
        .then(rels => loadRelatedEntities(id, rels))
        .then(related => vm.related = related);


    const involvementPromises = [
        involvementStore.findByEntityReference('CHANGE_INITIATIVE', id),
        involvementStore.findPeopleByEntityReference('CHANGE_INITIATIVE', id)
    ];
    $q.all(involvementPromises)
        .then(([relations, people]) => aggregatePeopleInvolvements(relations, people))
        .then(involvements => vm.involvements = involvements);
}


controller.$inject = [
    '$q',
    '$stateParams',
    'AppGroupStore',
    'BookmarkStore',
    'ChangeInitiativeStore',
    'InvolvementStore',
    'SourceDataRatingStore'
];


const page = {
    template: require('./change-initiative-view.html'),
    controller,
    controllerAs: 'ctrl'
};


export default page;

