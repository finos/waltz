
/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
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

