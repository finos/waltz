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


/**
 * @name waltz-measurable-rating-panel
 *
 * @description
 * This component render multiple <code>measurable-rating-panel</code> components
 * within a tab group.
 *
 * It is intended to be used to show measurables and ratings for a single application.
 */


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
        tab: null
    },
    byKind: {}
};


const descriptions = {
    BUSINESS_LINE: 'Which business lines this application supports',
    CAPABILITY: 'Which functions this application performs',
    PROCESS: 'Which processes this application supports',
    PRODUCT: 'Which products this application supports',
    REGION: 'Which regions this application services',
    SERVICE: 'Which services this application performs'
};

function groupByKind(measurables = [], ratings = []) {
    const allMeasurableKinds = _.keys(measurableKindNames);

    const measurablesByKind = _.groupBy(
        measurables,
        d => d.kind);

    const grouped = _.map(allMeasurableKinds, k => {
        const usedMeasurables = measurablesByKind[k] || [];

        const measurableIds = _.map(usedMeasurables, 'id');
        const ratingsForMeasure = _.filter(
            ratings,
            r => _.includes(measurableIds, r.measurableId));

        return {
            kind: {
                code: k,
                name: measurableKindNames[k],
                description: descriptions[k]
            },
            measurables: usedMeasurables,
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
            const firstNonEmptyTab = _.find(vm.tabs, t => t.ratings.length > 0);
            vm.visibility.tab = firstNonEmptyTab ? firstNonEmptyTab.kind.code : null;
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