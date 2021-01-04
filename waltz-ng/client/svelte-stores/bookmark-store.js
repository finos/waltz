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

import {writable} from "svelte/store";
import {CORE_API} from "../common/services/core-api-utils";
import {$http} from "../common/WaltzHttp";
import {initRemote} from "./remote";

function stripExtraneousFields(bookmark) {
    const strippedBookmark = _.pick(
        bookmark,
        ["id", "bookmarkKind", "url", "title", "isRestricted", "parent", "description", "lastUpdatedBy"]);
    return strippedBookmark;
}


const remote = initRemote("test");



export function mkBookmarkStore() {

    const load = (ref) => remote
        .fetchViewData("GET", `api/bookmarks/${ref.kind}/${ref.id}`);

    const remove = (bookmark) => $http
        .delete(`api/bookmarks/${bookmark.id}`)
        .then(r => load(bookmark.parent, true));

    const save = (bookmark) => {
        const strippedBookmark = stripExtraneousFields(bookmark);
        return $http
            .post("api/bookmarks", strippedBookmark)
            .then(r => load(bookmark.parent, true));
    }

    return {
        load,
        remove,
        save
    };
}
