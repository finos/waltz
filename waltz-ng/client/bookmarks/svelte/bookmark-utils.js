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

import {ascending} from "d3-array";
import {nest} from "d3-collection";
import * as enumUtils from "../../common/svelte/enum-utils";
import {toDomain} from "../../common/string-utils";
import _ from "lodash";


export function mkBookmarkKinds(nestedEnums, bookmarks = []) {
    const countsByKind = _.countBy(bookmarks, b => b.bookmarkKind);

    const enrichWithCount = bk => {
        const count = _.get(countsByKind, bk.key, 0);
        return Object.assign({}, bk, { count });
    };

    return _.chain(_.values(nestedEnums.BookmarkKind))
        .map(enrichWithCount)
        .orderBy(bk => bk.name)
        .value();
}


/**
 * returns bookmarks grouped by kind and sorted by title:
 *
 * ```
 *   [ { key: kind, value: [ ...bookmarks ] }, ... ]
 * ```
 * @param nestedEnums
 * @param bookmarks
 */
export function nestBookmarks(nestedEnums, bookmarks = []) {
    return nest()
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
}


function getIcon(nestedEnums, key) {
    return enumUtils.getIcon(nestedEnums, "BookmarkKind", key);
}


function getName(nestedEnums, key) {
    return enumUtils.getName(nestedEnums, "BookmarkKind", key);
}

export function filterBookmarks(xs, kind, qry) {
    return _
        .chain(xs)
        .filter(kind
            ? b => b.bookmarkKind === kind.key
            : () => true)
        .filter(_.isEmpty(qry)
            ? () => true
            : b => _.join([b.title, " ", b.url, " ", b.description]).toLowerCase().indexOf(_.toLower(qry)) > -1)
        .value();
}
