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
import {initialiseData} from '../../../common';
import {measurableKindNames} from  '../../../common/services/display-names';


const bindings = {
    ratings: '<',
    measurables: '<',
    sourceDataRatings: '<'
};


const template = require('./measurable-rating-app-section.html');


const initialState = {
    ratings: [],
    measurables: [],
    visibility: {
        overlay: false,
        tab: 0
    },
    byKind: {}
};

const descriptions = {
    BUSINESS_LINE: 'Which business lines this application supports',
    CAPABILITY: 'Which functions this application performs',
    PROCESS: 'Which processes this application supports',
    PRODUCT: 'Which products this application supports',
    REGION: 'Which region this application services',
    SERVICE: 'Which services this application performs'
};


function groupByKind(measurables = [], ratings = []) {
    const measurablesByKind = _.groupBy(
        measurables,
        d => d.kind);

    const grouped = _.map(measurablesByKind, (v, k) => {
        const measurableIds = _.map(v, 'id');
        const ratingsForMeasure = _.filter(
            ratings,
            r => _.includes(measurableIds, r.measurableId));

        return {
            kind: {
                code: k,
                name: measurableKindNames[k],
                description: descriptions[k]
            },
            measurables: v,
            ratings: ratingsForMeasure
        };
    });

    return _.sortBy(
        grouped,
        g => g.kind.name);
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (c) => {
        if (c.measurables || c.ratings) {
            vm.tabs = groupByKind(vm.measurables, vm.ratings);
        }
    };
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default component;