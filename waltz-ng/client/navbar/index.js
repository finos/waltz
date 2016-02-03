
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

import tmpl from './navbar.html';
import _ from 'lodash';

function controller(applicationStore, personStore, capabilityStore, orgUnitStore, timeout) {
    const searchResults = {
        show: false
    };

    function doSearch(query) {
        if (_.isEmpty(query)) {
            searchResults.show = false;
        } else {
            searchResults.show = true;
            applicationStore
                .search(query)
                .then(r => searchResults.apps = r);
            personStore
                .search(query)
                .then(r => searchResults.people = r);
            capabilityStore
                .search(query)
                .then(r => searchResults.capabilities = r);
            orgUnitStore
                .search(query)
                .then(r => searchResults.orgUnits = r);
        }
    }

    function dismissResults() {
        timeout(() => { searchResults.show = false; }, 400);
    }

    const vm = this;
    vm.doSearch = () => doSearch(vm.query);
    vm.showSearch = () => searchResults.show;
    vm.dismissResults = dismissResults;
    vm.searchResults = searchResults;
}

controller.$inject = ['ApplicationStore', 'PersonDataService', 'CapabilityStore', 'OrgUnitStore', '$timeout'];

export default (module) => module.directive('waltzNavbar', () => {
    return {
        restrict: 'E',
        template: tmpl,
        controller,
        controllerAs: 'ctrl'
    };
});
