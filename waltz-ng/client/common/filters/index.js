/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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