
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import _ from 'lodash';
import {initialiseData} from '../common';
import {measurableKindNames} from  '../common/services/display-names';
import {measurableKindDescriptions} from  '../common/services/descriptions';


const initialState = {

};


function prepareTabs(ratings = [], measurables = []) {
    const allMeasurableKinds = _.keys(measurableKindNames);
    const measurablesByKind = _.groupBy(measurables, 'kind');


    const tabs = _.map(allMeasurableKinds, k => {
        const measurablesForKind = measurablesByKind[k];
        const ids = _.map(measurablesForKind, 'id');
        const ratingsForKind = _.filter(ratings, r => _.includes(ids, r.measurableId))
        return {
            kind: {
                code: k,
                name: measurableKindNames[k],
                description: measurableKindDescriptions[k]
            },
            measurables: measurablesForKind,
            ratings: ratingsForKind
        };
    });

    return _.sortBy(
        tabs,
        g => g.kind.name);
}


function controller($q,
                    $stateParams,
                    applicationStore,
                    measurableStore,
                    measurableRatingStore) {

    const appId = $stateParams.id;
    const appRef = {
        kind: 'APPLICATION',
        id: appId
    };
    const vm = initialiseData(this, initialState);

    const measurablesPromise = measurableStore
        .findAll()
        .then(ms => vm.measurables = ms);

    const ratingsPromise = measurableRatingStore
        .findForEntityReference(appRef)
        .then(rs => vm.ratings = rs);

    applicationStore
        .getById(appId)
        .then(app => vm.application = app);

    $q.all([ratingsPromise, measurablesPromise])
        .then(() => vm.tabs = prepareTabs(vm.ratings, vm.measurables));

}


controller.$inject = [
    '$q',
    '$stateParams',
    'ApplicationStore',
    'MeasurableStore',
    'MeasurableRatingStore',
];


export default {
    template: require('./measurable-rating-edit.html'),
    controller,
    controllerAs: 'ctrl'
};
