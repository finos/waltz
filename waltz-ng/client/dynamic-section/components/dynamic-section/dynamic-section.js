/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import template from "./dynamic-section.html";
import {initialiseData} from "../../../common/index";
import {kindToViewState} from "../../../common/link-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";


const bindings = {
    parentEntityRef: "<",
    section: "<",
    onRemove: "<",
};

const initialState = {
    embedded: false,
    backLink: {
        state: "",
        params: {}
    },
    help: null
};


function controller($state, serviceBroker) {
    const vm = initialiseData(this, initialState);
    vm.embedded = _.startsWith($state.current.name, "embed");


    vm.$onChanges = () => {
        if (!_.isNil(vm.parentEntityRef)) {
            vm.backLink = {
                state: kindToViewState(vm.parentEntityRef.kind),
                params: {id: vm.parentEntityRef.id},
            };
            serviceBroker
                .loadAppData(CORE_API.StaticPanelStore.findAll)
                .then(r => {
                    const byId = _.keyBy(r.data, d => d.group);
                    const exact = `SECTION.HELP.${vm.section.componentId}.${vm.parentEntityRef.kind}`;
                    const vague = `SECTION.HELP.${vm.section.componentId}`;
                    const panel = byId[exact] || byId[vague] ;
                    if (panel) {
                        vm.help = panel.content;
                    }
                });
        }
    };

}


controller.$inject = [
    "$state",
    "ServiceBroker"
];


const component = {
    controller,
    template,
    bindings,
    transclude: true
};

const id = "waltzDynamicSection";


export default {
    id,
    component
};