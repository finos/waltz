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


/**
 * Measures have their own kinds.  Returning everything would be too much
 * @param all
 * @param id
 * @returns {*}
 */
function filterBySameMeasurableKind(all, id) {
    const measurable = _.find(all, { id });
    return measurable
        ? _.filter(all, m => m.kind === measurable.kind)
        : all;
}


function utils(appGroupStore,
               measurableStore,
               orgUnitStore) {

    const findAllForKind = (kind, id /* optional */) => {
        switch (kind) {
            case 'APP_GROUP':
                return appGroupStore
                    .findMyGroupSubscriptions()
                    .then(gs => _.map(gs, 'appGroup'));
            case 'MEASURABLE':
                return measurableStore
                    .findAll()
                    .then(all => filterBySameMeasurableKind(all, id));
            case 'ORG_UNIT':
                return orgUnitStore
                    .findAll();
            default :
                throw `esu: Cannot create hierarchy for kind - ${kind}`;
        }
    };

    return {
        findAllForKind
    };
}


utils.$inject = [
    'AppGroupStore',
    'MeasurableStore',
    'OrgUnitStore'
];


export default utils;