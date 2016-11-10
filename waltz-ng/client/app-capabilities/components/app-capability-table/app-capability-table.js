
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


const bindings = {
    appCapabilities: '<',
    capabilities: '<'
};


const template = require('./app-capability-table.html');


function refresh(appCapabilities = [], capabilities= []) {
    const capabilitiesById = _.keyBy(capabilities, 'id');

    return _.map(appCapabilities, ac => {
        return Object.assign(
            {},
            ac,
            { capability: capabilitiesById[ac.capabilityId] });
    });
}


function controller() {
    const vm = this;

    vm.$onChanges = () => {
        vm.items = refresh(vm.appCapabilities, vm.capabilities);
    };
}


const component =  {
    template,
    controller,
    bindings
};


export default component;
