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

const BINDINGS = {
    ratings: '='
};


function controller($scope) {

    const vm = this;

    vm.accuracyRatingToName = (rag) => {
        switch (rag) {
            case 'G':
                return "Trusted";
            case 'A':
                return "Indeterminate";
            case 'R':
                return "Untrusted";
            default:
                return "Unknown";
        }
    };

    vm.authoritativenessRatingToName = (rag) => {
        switch (rag) {
            case 'G':
                return "Golden";
            case 'A':
                return "Indirect";
            case 'R':
                return "'Found'";
            default:
                return "Unknown";
        }
    };

    vm.completenessRatingToName = (rag) => {
        switch (rag) {
            case 'G':
                return "Complete";
            case 'A':
                return "Partial";
            case 'R':
                return "Sparse";
            default:
                return "Unknown";
        }
    };



};

controller.$inject = ['$scope'];


export default () => ({
    replace: true,
    restrict: 'E',
    scope: {},
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller,
    template: require('./source-data-info.html')
});

