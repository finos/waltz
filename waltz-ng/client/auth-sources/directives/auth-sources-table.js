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
import _ from "lodash";

function controller() {

    const vm = this;

    vm.lookupOrgUnitName = (id) => {
        if (this.orgUnits) {
            const unit = _.keyBy(this.orgUnits, 'id')[id];
            return unit ? unit.name : '-';
        } else {
            return '-';
        }
    };

}

export default [
    () => ({
        restrict: 'E',
        replace: true,
        template: require('./auth-sources-table.html'),
        scope: {},
        controller,
        controllerAs: 'ctrl',
        bindToController: {
            authSources: '=',
            orgUnitId: '=',
            orgUnits: '='
        }
    })
];
