/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
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