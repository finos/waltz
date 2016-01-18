    /*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.bookmark.Bookmark;
import com.khartec.waltz.model.bookmark.BookmarkKind;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.service.bookmark.BookmarkService;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.WebUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Spark;

import static com.khartec.waltz.common.Checks.checkNotNull;

    @Service
public class BookmarksEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(BookmarksEndpoint.class);
    private static final String BASE_URL = WebUtilities.mkPath("api", "bookmarks");

    private final BookmarkService bookmarkService;
    private final ChangeLogService changeLogService;


    @Autowired
    public BookmarksEndpoint(BookmarkService service, ChangeLogService changeLogService) {
        checkNotNull(service, "bookmarkService must not be null");
        checkNotNull(changeLogService, "changeLogService must not be null");
        
        this.bookmarkService = service;
        this.changeLogService = changeLogService;
    }


    @Override
    public void register() {

        Spark.get(WebUtilities.mkPath(BASE_URL, ":kind", ":id"),
                (request, response) -> {
                    response.type(WebUtilities.TYPE_JSON);
                    EntityReference ref = WebUtilities.getEntityReference(request);
                    return bookmarkService.findByReference(ref);

                },
                WebUtilities.transformer);

        Spark.get(WebUtilities.mkPath(BASE_URL, "types"),
                (request, response) -> {
                    response.type(WebUtilities.TYPE_JSON);
                    return BookmarkKind.values();
                },
                WebUtilities.transformer);

        Spark.post(WebUtilities.mkPath(BASE_URL),
                (request, response) -> {
                    response.type(WebUtilities.TYPE_JSON);
                    Bookmark bookmark = WebUtilities.readBody(request, Bookmark.class);

                    LOG.info("Saving bookmark: "+bookmark);
                    boolean isUpdate = bookmark.id().isPresent();

                    return isUpdate
                            ? bookmarkService.update(bookmark)
                            : bookmarkService.create(bookmark);
                },
                WebUtilities.transformer);


        Spark.delete(WebUtilities.mkPath(BASE_URL, ":id"), (request, response) -> {
            response.type(WebUtilities.TYPE_JSON);

            long bookmarkId = WebUtilities.getId(request);

            Bookmark bookmark = bookmarkService.getById(bookmarkId);
            if (bookmark == null) {
                LOG.warn("Attempt made to delete non-existent bookmark: " + bookmarkId);
                return false;
            }

            LOG.info("Deleting bookmark: " + bookmark);

            changeLogService.write(ImmutableChangeLog.builder()
                    .message("Deleted bookmark: " + bookmark.title().orElse("?") + " / " +bookmark.kind())
                    .parentReference(bookmark.parent())
                    .userId(WebUtilities.getUser(request).userName())
                    .severity(Severity.INFORMATION)
                    .build());

            return bookmarkService.deleteById(bookmarkId);

        }, WebUtilities.transformer);

    }


    @Override
    public String toString() {
        return "BookmarksEndpoint{" +
                "bookmarkService=" + bookmarkService +
                ", changeLogService=" + changeLogService +
                '}';
    }
}
