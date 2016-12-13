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

const initData = {
    ratings: []
};

function controller($scope, sourceDataRatingStore) {
    const vm = Object.assign(this, initData);

    sourceDataRatingStore
        .findAll()
        .then(ratings => vm.ratings = ratings);
}

controller.$inject = [
    '$scope', 'SourceDataRatingStore'
];


export default {
    template: require('./list.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};
