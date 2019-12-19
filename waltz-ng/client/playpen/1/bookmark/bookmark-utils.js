
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

import {getIcon, getName} from "./enum-utils";
import {ascending} from "d3-array";
import {nest} from "d3-collection";


export const toDomain = url => "www.google.com";


export const mkBookmarkKinds = (nestedEnums, bookmarks = []) => {
    const countsByKind = _.countBy(bookmarks, b => b.bookmarkKind);

    const enrichWithCount = bk => {
        const count = _.get(countsByKind, bk.key, 0);
        return Object.assign({}, bk, { count });
    };

    return _.chain(_.values(nestedEnums.BookmarkKind))
        .map(enrichWithCount)
        .orderBy(bk => bk.name)
        .value();
};

/**
 * returns bookmarks grouped by kind and sorted by title:
 *
 * ```
 *   [ { key: kind, value: [ ...bookmarks ] }, ... ]
 * ```
 * @param nestedEnums
 * @param bookmarks
 */
export const nestBookmarks = (nestedEnums, bookmarks = []) => nest()
    .key(d => d.bookmarkKind)
    .sortKeys((a, b) => ascending(
        getName(nestedEnums, a),
        getName(nestedEnums, b)))
    .sortValues((a, b) => ascending(a.title, b.title))
    .rollup(xs => _.map(xs, x => {
        const extension = {
            domain: toDomain(x.url),
            icon: getIcon(nestedEnums, x.bookmarkKind)
        };
        return Object.assign({}, x, extension);
    }))
    .entries(bookmarks);
