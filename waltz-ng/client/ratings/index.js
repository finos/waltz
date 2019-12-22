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

import angular from "angular";
import * as RatingSchemeStore from "./services/rating-scheme-store";
import {registerComponents, registerStores} from "../common/module-utils";
import RatingPicker from "./components/rating-picker/rating-picker";
import RatingIndicatorCell from "./components/rating-indicator-cell/rating-indicator-cell";
import RatingSchemeLegend from "./components/rating-scheme-legend/rating-scheme-legend";
import RagLine from "./components/rag-line/rag-line";

export default () => {
    const module = angular.module("waltz.ratings", []);

    registerComponents(module, [
        RatingIndicatorCell,
        RagLine,
        RatingPicker,
        RatingSchemeLegend
    ]);

    registerStores(module, [ RatingSchemeStore ]);

    return module.name;
};
