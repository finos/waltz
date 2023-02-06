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

import services from "./services";
import Routes from "./routes";
import {registerComponents} from "../common/module-utils";
import AssessmentDefinitionList from "./pages/list/assessment-definition-list";
import AssessmentRatingFavouritesList from "./components/favourites-list/assessment-rating-favourites-list";
import AssessmentRatingList from "./components/list/assessment-rating-list";
import AssessmentRatingSection from "./components/section/assessment-rating-section";
import AssessmentRatingSubSection from "./components/sub-section/assessment-rating-sub-section";
import AssessmentRatingSummaryPies from "./components/summary-pies/assessment-rating-summary-pies";
import AssessmentRatingTrafficLights from "./components/traffic-lights/assessment-rating-traffic-lights";
import BulkAssessmentRatingSelector from "./components/bulk-assessment-rating-selector/bulk-assessment-rating-selector";
import AssessmentDefinitionOverview from "./components/assessment-definition-overview/assessment-definition-overview";
import AssessmentInfoTile from "./components/info-tile/assessment-info-tile";
import {entity} from "../common/services/enums/entity";
import _ from "lodash";
import {writable} from "svelte/store";
import {assessmentDefinitionStore} from "../svelte-stores/assessment-definition";




function createFavouriteStore() {
    const initialValues = _.reduce(
        entity,
        (acc, e) => { acc[e.key] = []; return acc;},
        {});

    function replaceData(r) {
        return set(_.groupBy(r.data, d => d.entityKind));
    }

    const {subscribe, set, update} = writable(initialValues);

    assessmentDefinitionStore
        .findFavouritesForUser()
        .subscribe(replaceData);

    return {
        subscribe,
        add: function(defId) {
            assessmentDefinitionStore
                .addFavourite(defId)
                .then(replaceData);
        },
        remove: function(defId) {
            assessmentDefinitionStore
                .removeFavourite(defId)
                .then(replaceData);
        }
    }
}


export const favouriteAssessmentStore = createFavouriteStore();


export default () => {

    const module = angular.module("waltz.assessments", []);
    module.config(Routes);

    services(module);

    registerComponents(module, [
        AssessmentDefinitionList,
        AssessmentRatingFavouritesList,
        AssessmentRatingList,
        AssessmentRatingSubSection,
        AssessmentRatingSummaryPies,
        AssessmentRatingTrafficLights,
        BulkAssessmentRatingSelector,
        AssessmentDefinitionOverview,
        AssessmentRatingSection,
        AssessmentInfoTile
    ]);

    return module.name;
};
