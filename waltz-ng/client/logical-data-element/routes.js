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

import {CORE_API} from "../common/services/core-api-utils";
import LogicalDataElementView from './logical-data-element-view';

const baseState = {
};


const viewState = {
    url: 'logical-data-element/{id:int}',
    views: {'content@': LogicalDataElementView },
};


function bouncer($state, $stateParams, serviceBroker) {
    const externalId = $stateParams.externalId;
    serviceBroker
        .loadViewData(CORE_API.LogicalDataElementStore.getByExternalId, [externalId])
        .then(r => {
            const element = r.data;
            if(element) {
                $state.go('main.logical-data-element.view', {id: element.id});
            } else {
                console.log(`Cannot find logical data element corresponding to external id: ${externalId}`);
            }
        });
}

bouncer.$inject = ['$state', '$stateParams', 'ServiceBroker'];


const bouncerState = {
    url: 'logical-data-element/external-id/{externalId:string}',
    resolve: {
        bouncer
    }
};

function setup($stateProvider) {

    $stateProvider
        .state('main.logical-data-element.bouncer', bouncerState)
        .state('main.logical-data-element', baseState)
        .state('main.logical-data-element.view', viewState);
}


setup.$inject = ['$stateProvider'];


export default setup;