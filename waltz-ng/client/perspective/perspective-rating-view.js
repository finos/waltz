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
import template from './perspective-rating-view.html';
import {mkOverrides} from './perpective-utilities';


function controller($q,
                    $stateParams,
                    applicationStore,
                    measurableStore,
                    measurableRatingStore,
                    perspectiveDefinitionStore,
                    perspectiveRatingStore) {

    const vm = this;

    const entityReference = {
        id: $stateParams.entityId,
        kind: $stateParams.entityKind
    };

    const idSelector = {
        entityReference,
        scope: 'EXACT'
    };

    const defnPromise = perspectiveDefinitionStore
        .findAll()
        .then(ps => {
            vm.perspectives = ps;
        });


    applicationStore
        .getById(entityReference.id)
        .then(app => {
            vm.application = app;
            vm.entityReference = Object.assign({}, entityReference, { name: app.name });
        });

    const ratingPromise = perspectiveRatingStore
        .findForEntity(entityReference)
        .then(rs => {
            vm.perspectiveRatings = rs;
        });

    const measurablePromise = measurableStore
        .findMeasurablesBySelector(idSelector)
        .then(measurables => vm.measurables = measurables);

    measurableRatingStore
        .findByAppSelector(idSelector)
        .then(ratings => vm.measurableRatings = ratings);

    $q.all([ratingPromise, measurablePromise, defnPromise])
        .then(() => {
            vm.perspectiveBlocks = _
                .chain(vm.perspectives)
                .map(p => {
                    return {
                        definition: p,
                        measurablesX: _.filter(vm.measurables, { categoryId: p.categoryX }),
                        measurablesY: _.filter(vm.measurables, { categoryId: p.categoryY })
                    };
                })
                .filter(pb => pb.measurablesX.length > 0 && pb.measurablesY.length > 0)
                .map(pb => {
                    const relevantMeasurableIds = _.union(_.map(pb.measurablesX, "id"), _.map(pb.measurablesY, "id"));
                    const relevantRatings = _.filter(vm.perspectiveRatings, r => {
                        const xRelevant = _.includes(relevantMeasurableIds, r.value.measurableX);
                        const yRelevant = _.includes(relevantMeasurableIds, r.value.measurableY);
                        return xRelevant && yRelevant;
                    });
                    const overrides = mkOverrides(relevantRatings);
                    const result = Object.assign({}, pb, { overrides });
                    return result;
                })
                .value();
        })


}



controller.$inject = [
    '$q',
    '$stateParams',
    'ApplicationStore',
    'MeasurableStore',
    'MeasurableRatingStore',
    'PerspectiveDefinitionStore',
    'PerspectiveRatingStore'
];


const page = {
    controller,
    controllerAs: 'ctrl',
    template
};

export default page;