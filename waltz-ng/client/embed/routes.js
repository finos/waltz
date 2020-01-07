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

import _ from "lodash";
import embedView from "./pages/embed-view";
import {dynamicSections} from "../dynamic-section/dynamic-section-definitions";
import {loadByExtId} from "../common/entity-utils";


function resolveSection(sectionComponentId) {
    const section = _.find(
        _.values(dynamicSections),
        section => _.isEqual(section.componentId, sectionComponentId));
    return section;
}


function goToNotFound($state) {
    $state.go("embed.not-found", {}, {location: false});
}


function bouncer($q, $state, $stateParams, serviceBroker) {
    const {kind, extId, sectionComponentId} = $stateParams;
    const section = resolveSection(sectionComponentId);
    if (!section) {
        goToNotFound($state);
        return;
    }

    loadByExtId(serviceBroker, kind, extId)
        .then(r => {
            const entity = _.first(r);
            if (entity) {
                const id = entity.id;
                $state.go("embed.internal", {kind, id, sectionId: section.id}, { location: false });
            } else {
                goToNotFound($state);
            }
        });
}


bouncer.$inject = [
    "$q",
    "$state",
    "$stateParams",
    "ServiceBroker"
];


const notFoundView = {
    template: "<h4>Sorry, cannot render that embedded section</h4>"
};


function setup($stateProvider) {
    $stateProvider
        .state("embed", {})
        .state("embed.internal", {
            url: "/embed/internal/{kind:string}/{id:int}/{sectionId:int}",
            views: { "all@": embedView }
        })
        .state("embed.not-found", {
            url: "/embed/not-found",
            views: { "all@": notFoundView }
        })
        .state("embed.external", {
            url: "/embed/external/{kind:string}/{extId:string}/{sectionComponentId:string}",
            resolve: {bouncer}
        });
}


setup.$inject = [
    "$stateProvider"
];


export default setup;