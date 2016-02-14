
/*
 *  This file is part of Waltz.
 *
 *  Waltz is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Waltz is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 *
 */



const baseState = {
    url: 'app-group'
};


const viewState = {
    url: '/:id',
    views: { 'content@': require('./app-group-view') }
};


const editState = {
    url: '/:id/edit',
    views: { 'content@': require('./app-group-edit') }
};


function setupRoutes($stateProvider) {
    $stateProvider
        .state('main.app-group', baseState)
        .state('main.app-group.view', viewState)
        .state('main.app-group.edit', editState);
}

setupRoutes.$inject = ['$stateProvider'];


export default (module) => {

    module.service('AppGroupStore', require('./services/app-group-store'));
    module.directive('waltzAppGroupList', require('./directives/app-group-list'));
    module.directive('waltzAppGroupListSection', require('./directives/app-group-list-section'));
    module.directive('waltzAppGroupAppSelectionList', require('./directives/app-group-app-selection-list'));
    module.directive('waltzAppGroupSummary', require('./directives/app-group-summary'));


    module.config(setupRoutes);
}