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
import {initialiseData} from "../../common";


const bindings = {
    authSources: '<',
    orgUnitId: '<',
    orgUnitRefs: '<'
};


const initialState = {
    authSources: [],
    orgUnitRefs: []
};


const template = require('./auth-sources-table.html');


function controller() {

    const vm = initialiseData(this, initialState);

    vm.lookupOrgUnitName = (id) => {
        if (!_.isEmpty(vm.orgUnitRefs)) {
            const unit = _.keyBy(vm.orgUnitRefs, o => o.entityReference.id)[id];
            return unit ? unit.entityReference.name : '-';
        } else {
            return '-';
        }
    };

}


controller.$inject = [];


const component = {
    bindings,
    controller,
    template
};


export default component;