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

import {remote} from "./remote";
import {toEntityRef} from "../common/entity-utils";


function stripExtraneousFields(bookmark) {
    const simplifiedBookmark = _.pick(bookmark, ["id", "bookmarkKind", "url", "title", "isRestricted", "parent", "description", "lastUpdatedBy"]);
    const simplifiedParent = {parent: toEntityRef(bookmark.parent)};
    return Object.assign(
        {},
        simplifiedBookmark,
        simplifiedParent);
}


export function mkBookmarkStore() {
    const load = (ref, force) => remote
        .fetchViewList(
            "GET",
            `api/bookmarks/${ref.kind}/${ref.id}`,
            null,
            {force});

    const remove = (bookmark) => remote
        .execute(
            "DELETE",
            `api/bookmarks/${bookmark.id}`)
        .then(r => load(bookmark.parent, true));

    const save = (bookmark) => remote
        .execute(
            "POST",
            "api/bookmarks",
            stripExtraneousFields(bookmark))
        .then(r => load(bookmark.parent, true));

    return {
        load,
        remove,
        save
    };
}

export const bookmarkStore = mkBookmarkStore();