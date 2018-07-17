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

import template from './recalculate-view.html';


function controller(notification,
                    authSourceStore,
                    complexityStore,
                    dataTypeUsageStore) {
    const vm = this;


    vm.recalcFlowRatings = () => {
        notification.info('Flow Ratings recalculation requested');
        authSourceStore
            .recalculateAll()
            .then(() => notification.success('Flow Ratings recalculated'));
    };

    vm.recalcDataTypeUsages = () => {
        notification.info('Data Type Usage recalculation requested');
        dataTypeUsageStore
            .recalculateAll()
            .then(() => notification.success('Data Type Usage recalculated'));
    };

    vm.recalcComplexity = () => {
        notification.info('Complexity recalculation requested');
        complexityStore
            .recalculateAll()
            .then(() => notification.success('Complexity recalculated'));
    };
}


controller.$inject = [
    'Notification',
    'AuthSourcesStore',
    'ComplexityStore',
    'DataTypeUsageStore'
];


const page = {
    template,
    controller,
    controllerAs: 'ctrl'
};


export default page;