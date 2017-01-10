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

const baseState = {
    url: 'entity-statistic'
};


const personViewState = {
    url: '/PERSON/{id:int}/{statId:int}',
    views: { 'content@': require('./person-entity-statistic-view') }
};

const genericViewState = {
    url: '/{kind:string}/{id:int}/{statId:int}',
    views: { 'content@': require('./entity-statistic-view') }
};


function setupRoutes($stateProvider) {
    $stateProvider
        .state('main.entity-statistic', baseState)
        .state('main.entity-statistic.view-person', personViewState)
        .state('main.entity-statistic.view', genericViewState);
}

setupRoutes.$inject = ['$stateProvider'];


export default setupRoutes;