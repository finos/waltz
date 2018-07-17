
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

import _ from 'lodash';
import {nest} from 'd3-collection'
import template from './tag-explorer.html';

function controller($stateParams, entityTagStore) {

    const vm = this;
    vm.tagSets = [];

    const addTagSet = (selectedTag) => {
        entityTagStore
            .findByTag(selectedTag)
            .then(r => {
                const tagSet = {
                    tag: selectedTag,
                    refs: r,
                    refsByKind: nest()
                        .key(d => d.kind)
                        .entries(r)
                };
                vm.tagSets.push(tagSet);
            });
    };

    const removeTagSet = (selectedTagSet) => {
        vm.tagSets = _.reject(vm.tagSets, ts => ts === selectedTagSet);
    };

    entityTagStore
        .findAllTags()
        .then(r => {
            vm.tags = r;
        });

    vm.addTagSet = addTagSet;
    vm.removeTagSet = removeTagSet;

    addTagSet($stateParams.tag);
}

controller.$inject = ['$stateParams', 'EntityTagStore'];


export default {
    template,
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};

