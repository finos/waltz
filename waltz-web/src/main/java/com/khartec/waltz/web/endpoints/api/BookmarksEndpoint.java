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

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.bookmark.Bookmark;
import com.khartec.waltz.model.user.Role;
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
                    requireRole(userRoleService, request, Role.BOOKMARK_EDITOR);

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
            requireRole(userRoleService, request, Role.BOOKMARK_EDITOR);

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
