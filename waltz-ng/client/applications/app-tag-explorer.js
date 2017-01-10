
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

import _ from 'lodash';

function controller($stateParams, appStore) {


    const vm = this;
    vm.tagSets = [];

    const addTagSet = (selectedTag) => {
        appStore.findByTag(selectedTag).then(r => {
            vm.tagSets.push({ tag: selectedTag, apps: r});
        });
    };

    const removeTagSet = (selectedTagSet) => {
        vm.tagSets = _.reject(vm.tagSets, ts => ts === selectedTagSet);
    };

    appStore.findAllTags().then(r => {
        vm.tags = r;
    });

    vm.addTagSet = addTagSet;
    vm.removeTagSet = removeTagSet;

    addTagSet($stateParams.tag);
}

controller.$inject = ['$stateParams', 'ApplicationStore'];

export default {
    template: require('./app-tag-explorer.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};

