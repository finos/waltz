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
import {initialiseData, invokeFunction} from "../../common";


const bindings = {
    allActors: '<',
    onSelect: '<'
};


const template = require('./actor-selector.html');


const initialState = {
    allActors: [],
    actors: []
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.refresh = function (query) {
        if (!query) return;
        const queryLc = _.lowerCase(query);
        vm.actors = _.filter(vm.allActors, (a) => _.startsWith(_.lowerCase(a.name), queryLc));
    };

    vm.select = (item) => {
        invokeFunction(vm.onSelect, Object.assign(item, {kind: 'ACTOR'}));
    };
}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;


