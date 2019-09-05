/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";
import embedView from "./pages/embed-view";
import {CORE_API} from "../common/services/core-api-utils";
import {dynamicSections} from "../dynamic-section/dynamic-section-definitions";


const externalIdLookupMethods = {
    APPLICATION: CORE_API.ApplicationStore.findByAssetCode,
    MEASURABLE: CORE_API.MeasurableStore.findByExternalId
};


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
    const lookupMethod = externalIdLookupMethods[kind];
    if (!section || !lookupMethod) {
        goToNotFound($state);
        return;
    }

    serviceBroker.loadViewData(lookupMethod, [ extId] )
        .then(r => {
            const entity = _.first(r.data);
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