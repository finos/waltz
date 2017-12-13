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


function controller($stateParams,
                    applicationStore,
                    measurableStore,
                    measurableRatingStore,
                    perspectiveDefinitionStore,
                    perspectiveRatingStore,
                    staticPanelStore)
{
    const vm = initialiseData(this, initialState);

    const perspectiveId = $stateParams.perspectiveId;
    const applicationId = $stateParams.entityId;

    const entityReference = {
        id: applicationId,
        kind: 'APPLICATION'
    };

    const idSelector = {
        entityReference,
        scope: 'EXACT'
    };

    staticPanelStore
        .findByGroup('PERSPECTIVE.RATING.EDIT')
        .then(panels => vm.panels = panels);

    applicationStore
        .getById(applicationId)
        .then(app => {
            vm.application = app;
            vm.entityReference = Object.assign({}, entityReference, { name: app.name });
        });

    perspectiveDefinitionStore
        .findAll()
        .then(pds => vm.perspectiveDefinition = _.find(pds, { id: perspectiveId || 1 }))
        .then(pd => perspectiveRatingStore.findForEntityAxis(pd.categoryX, pd.categoryY, entityReference))
        .then(rs => vm.perspectiveRatings = rs);

    measurableStore
        .findMeasurablesBySelector(idSelector)
        .then(measurables => vm.measurables = measurables);

    measurableRatingStore
        .findByAppSelector(idSelector)
        .then(ratings => vm.measurableRatings = ratings);

    vm.save = (values = []) => {
        const p = vm.perspectiveDefinition;
        const withoutUnknowns = _.reject(values, { rating: 'Z' });
        return perspectiveRatingStore
            .updateForEntityAxis(p.categoryX, p.categoryY, entityReference, withoutUnknowns)
            .then(() => perspectiveRatingStore.findForEntityAxis(p.categoryX, p.categoryY, entityReference))
            .then(rs => vm.perspectiveRatings = rs);
    };
}


/**
 * @name waltz-perspective-rating-edit
 *
 * @description
 */


const initialState = {};


const template = require('./perspective-rating-edit.html');


controller.$inject = [
    '$stateParams',
    'ApplicationStore',
    'MeasurableStore',
    'MeasurableRatingStore',
    'PerspectiveDefinitionStore',
    'PerspectiveRatingStore',
    'StaticPanelStore'
];


const page = {
    template,
    controller,
    controllerAs: 'ctrl'
};


export default page;