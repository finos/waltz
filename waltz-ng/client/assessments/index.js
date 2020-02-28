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
import { registerComponents } from "../common/module-utils";
import AssessmentEditor from "./components/editor/assessment-editor";
import AssessmentRatingList from "./components/list/assessment-rating-list";
import AssessmentRatingSubSection from "./components/sub-section/assessment-rating-sub-section";
import AssessmentRatingSummaryPies from "./components/summary-pies/assessment-rating-summary-pies";
import AssessmentRatingTrafficLights from "./components/traffic-lights/assessment-rating-traffic-lights";

export default () => {

    const module = angular.module("waltz.assessments", []);

    services(module);

    registerComponents(module, [
        AssessmentEditor,
        AssessmentRatingList,
        AssessmentRatingSubSection,
        AssessmentRatingSummaryPies,
        AssessmentRatingTrafficLights
    ]);

    return module.name;
};
