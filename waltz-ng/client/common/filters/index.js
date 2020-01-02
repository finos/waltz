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

import IsEmptyFilter from "./is-empty-filter";
import MergeFilter from "./merge-filter";
import ToBasisOffsetFilter from "./to-basis-offset-filter";
import DisplayNameFilter from "./display-name-filter";
import ToDomainFilter from "./to-domain-filter";
import DescriptionFilter from "./description-filter";
import FixedFilter from "./fixed-filter";
import IconNameFilter from "./icon-name-filter";
import TruncateFilter from "./truncate-filter";
import TruncateMiddleFilter from "./truncate-middle-filter";
import UseDefaultRatingSchemeFilter from "./use-default-rating-scheme-filter";


export default (module) => {
    module
        .filter('isEmpty', IsEmptyFilter)
        .filter('merge', MergeFilter)
        .filter('useDefaultRatingScheme', UseDefaultRatingSchemeFilter)
        .filter('toBasisOffset', ToBasisOffsetFilter)
        .filter('toDisplayName', DisplayNameFilter)
        .filter('toDomain', ToDomainFilter)
        .filter('toDescription', DescriptionFilter)
        .filter('toFixed', FixedFilter)
        .filter('toIconName', IconNameFilter)
        .filter('truncate', TruncateFilter)
        .filter('truncateMiddle', TruncateMiddleFilter);
};