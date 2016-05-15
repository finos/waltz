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

function controller(personStore, $q) {
    const vm = this;

    vm.people = [];
    vm.refresh = (query) => {
        if (!query) return $q.resolve([]);
        return personStore
            .search(query)
            .then((people) => {
                vm.people = people;
            });
    };
}

controller.$inject = [
    'PersonStore',
    '$q'
];

export default () => {
    return {
        restrict: 'E',
        replace: true,
        template: require('./person-selector.html'),
        scope: {},
        bindToController: {
            model: '='
        },
        controller,
        controllerAs: 'ctrl'
    };
};

