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
import { CORE_API } from '../../common/services/core-api-utils';


/**
 * Measures have their own kinds.  Returning everything would be too much
 * @param all
 * @param id
 * @returns {*}
 */
function filterBySameMeasurableCategory(all, id) {
    const measurable = _.find(all, { id });
    return measurable
        ? _.filter(all, m => m.categoryId === measurable.categoryId)
        : all;
}


function utils(serviceBroker) {

    const findAllForKind = (kind, id /* optional */) => {
        switch (kind) {
            case 'APP_GROUP':
                return serviceBroker
                    .loadViewData(CORE_API.AppGroupStore.findMyGroupSubscriptions, [])
                    .then(r => _.map(r.data, 'appGroup'));
            case 'MEASURABLE':
                return serviceBroker
                    .loadAppData(CORE_API.MeasurableStore.findAll, [])
                    .then(r => filterBySameMeasurableCategory(r.data, id));
            case 'ORG_UNIT':
                return serviceBroker
                    .loadAppData(CORE_API.OrgUnitStore.findAll, [])
                    .then(r => r.data);
            case 'FLOW_DIAGRAM':
                return serviceBroker
                    .loadViewData(CORE_API.FlowDiagramStore.getById, [id])
                    .then(r => [r.data]);
            case 'CHANGE_INITIATIVE':
                return serviceBroker
                    .loadViewData(CORE_API.ChangeInitiativeStore.getById, [id])
                    .then(r => [r.data]);
            default :
                throw `esu: Cannot create hierarchy for kind - ${kind}`;
        }
    };

    return {
        findAllForKind
    };
}


utils.$inject = [
    'ServiceBroker'
];


export default utils;