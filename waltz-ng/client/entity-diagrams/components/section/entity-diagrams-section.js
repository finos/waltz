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
import {initialiseData} from "../../../common";
import _ from "lodash";
import template from "./entity-diagrams-section.html";
import AggregateOverlayDiagramPanel from "../../../aggregate-overlay-diagram/components/panel/AggregateOverlayDiagramPanel.svelte"
import EntityDiagramPanel from "../entity-overlay-diagrams/EntityDiagramPanel.svelte";

const bindings = {
    parentEntityRef: "<"
};


const TABS = {
    linked: {
        id: "linked",
        name: "Linked",
        disallowedKinds: ["PERSON", "ORG_UNIT", "APP_GROUP", "FLOW_DIAGRAM", "PROCESS_DIAGRAM"]
    },
    overlay: {
        id: "overlay",
        name: "Overlay",
        disallowedKinds: ["APPLICATION", "ACTOR", "PHYSICAL_FLOW", "LOGICAL_DATA_FLOW", "PHYSICAL_SPECIFICATION"]
    },
    entity: {
        id: "entity",
        name: "Entity",
        disallowedKinds: ["APPLICATION", "ACTOR", "PHYSICAL_FLOW", "LOGICAL_DATA_FLOW", "PHYSICAL_SPECIFICATION"]
    }
};


const initialState = {
    AggregateOverlayDiagramPanel,
    EntityDiagramPanel,
    tab: null,
    TABS
};

function determineStartingTab(ref) {
    if (isDisallowed(TABS.overlay, ref)) {
        return TABS.linked;
    }
    if (isDisallowed(TABS.linked, ref)) {
        return TABS.overlay;
    }
    return TABS.linked;
}

function isDisallowed(tab, ref) {
    return _.includes(tab.disallowedKinds, ref.kind)
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        vm.tab = determineStartingTab(vm.parentEntityRef).id;
    };

    vm.isDisabled = (tab) => {
        return isDisallowed(tab, vm.parentEntityRef) ;
    };
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzEntityDiagramsSection"
};
