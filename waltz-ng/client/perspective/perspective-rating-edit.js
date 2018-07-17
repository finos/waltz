/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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
import _ from "lodash";
import {initialiseData} from "../common";
import {CORE_API} from "../common/services/core-api-utils";
import template from './perspective-rating-edit.html';


function controller($stateParams,
                    serviceBroker)
{
    const vm = initialiseData(this, initialState);

    const perspectiveId = $stateParams.perspectiveId;
    const entityId = $stateParams.entityId;
    const entityKind = $stateParams.entityKind;
    const entityReference = { id: entityId, kind: entityKind };
    const selector = { entityReference, scope: 'EXACT'};

    const loadEntityCallByKind = {
        'APPLICATION': CORE_API.ApplicationStore.getById,
        'ACTOR': CORE_API.ActorStore.getById
    };

    const loadPerspectiveRatings = (pd) => {
        return serviceBroker
            .loadViewData(
                CORE_API.PerspectiveRatingStore.findForEntityAxis,
                [ pd.categoryX, pd.categoryY, entityReference],
                { force: true })
            .then(r => vm.perspectiveRatings = r.data);
    };

    serviceBroker
        .loadViewData(loadEntityCallByKind[entityKind], [ entityReference.id ])
        .then(r => vm.entityReference = r.data);

    serviceBroker
        .loadAppData(CORE_API.PerspectiveDefinitionStore.findAll)
        .then(r => r.data)
        .then(pds => vm.perspectiveDefinition = _.find(pds, { id: perspectiveId || 1 }))
        .then(pd => loadPerspectiveRatings(pd));

    serviceBroker
        .loadViewData(CORE_API.MeasurableStore.findMeasurablesBySelector, [ selector ])
        .then(r => vm.measurables = r.data);

    serviceBroker
        .loadViewData(CORE_API.MeasurableRatingStore.findForEntityReference, [ entityReference ])
        .then(r => vm.measurableRatings = r.data);

    vm.save = (values = []) => {
        const pd = vm.perspectiveDefinition;
        const withoutUnknowns = _.reject(values, { rating: 'Z' });
        return serviceBroker
            .execute(CORE_API.PerspectiveRatingStore.updateForEntityAxis, [pd.categoryX, pd.categoryY, entityReference, withoutUnknowns])
            .then(() => loadPerspectiveRatings(pd));
    };
}


/**
 * @name waltz-perspective-rating-edit
 *
 * @description
 */


const initialState = {};


controller.$inject = [
    '$stateParams',
    'ServiceBroker'
];


const page = {
    template,
    controller,
    controllerAs: 'ctrl'
};


export default page;