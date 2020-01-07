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

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.bookmark.Bookmark;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.bookmark.BookmarkService;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static spark.Spark.*;

@Service
public class BookmarksEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(BookmarksEndpoint.class);
    private static final String BASE_URL = mkPath("api", "bookmarks");

    private final BookmarkService bookmarkService;
    private final ChangeLogService changeLogService;
    private final UserRoleService userRoleService;


    @Autowired
    public BookmarksEndpoint(BookmarkService service,
                             ChangeLogService changeLogService,
                             UserRoleService userRoleService) {
        checkNotNull(service, "bookmarkService must not be null");
        checkNotNull(changeLogService, "changeLogService must not be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.bookmarkService = service;
        this.changeLogService = changeLogService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {

        get(mkPath(BASE_URL, ":kind", ":id"),
                (request, response) -> {
                    response.type(TYPE_JSON);
                    EntityReference ref = getEntityReference(request);
                    return bookmarkService.findByReference(ref);

                },
                transformer);

        post(mkPath(BASE_URL),
                (request, response) -> {
                    requireRole(userRoleService, request, SystemRole.BOOKMARK_EDITOR);

                    response.type(TYPE_JSON);
                    Bookmark bookmark = readBody(request, Bookmark.class);

                    LOG.info("Saving bookmark: "+bookmark);
                    boolean isUpdate = bookmark.id().isPresent();

                    return isUpdate
                            ? bookmarkService.update(bookmark, getUsername(request))
                            : bookmarkService.create(bookmark, getUsername(request));
                },
                transformer);


        delete(mkPath(BASE_URL, ":id"), (request, response) -> {
            requireRole(userRoleService, request, SystemRole.BOOKMARK_EDITOR);

            response.type(TYPE_JSON);

            long bookmarkId = getId(request);

            Bookmark bookmark = bookmarkService.getById(bookmarkId);
            if (bookmark == null) {
                LOG.warn("Attempt made to delete non-existent bookmark: " + bookmarkId);
                return false;
            }

            LOG.info("Deleting bookmark: " + bookmark);
            return bookmarkService.deleteById(bookmark, getUsername(request));

        }, transformer);

    }


    @Override
    public String toString() {
        return "BookmarksEndpoint{" +
                "bookmarkService=" + bookmarkService +
                ", changeLogService=" + changeLogService +
                '}';
    }
}
