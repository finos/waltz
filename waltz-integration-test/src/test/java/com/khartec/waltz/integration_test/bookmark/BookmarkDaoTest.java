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

package com.khartec.waltz.integration_test.bookmark;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.data.bookmark.BookmarkDao;
import com.khartec.waltz.integration_test.BaseIntegrationTest;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.bookmark.Bookmark;
import com.khartec.waltz.model.bookmark.ImmutableBookmark;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.khartec.waltz.common.CollectionUtilities.first;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BookmarkDaoTest extends BaseIntegrationTest {
    private static final AtomicLong refCounter = new AtomicLong();
    private final BookmarkDao dao = ctx.getBean(BookmarkDao.class);


    @Test
    public void bookmarkCanBeCreated() {
        EntityReference bookmarkedEntity = mkAppRef();
        Bookmark bookmark = createBookmark(bookmarkedEntity, "test bookmark", dao);
        List<Bookmark> bookmarks = dao.findByReference(bookmarkedEntity);
        assertEquals(1, bookmarks.size());
        assertEquals(bookmark, first(bookmarks));
    }


    @Test
    public void bookmarksCanBeCreated() {
        BookmarkDao dao = ctx.getBean(BookmarkDao.class);
        EntityReference bookmarkedEntity = mkAppRef();

        Bookmark bookmark1 = createBookmark(bookmarkedEntity, "test bookmark1", dao);
        Bookmark bookmark2 = createBookmark(bookmarkedEntity, "test bookmark2", dao);

        List<Bookmark> bookmarks = dao.findByReference(bookmarkedEntity);
        assertEquals(2, bookmarks.size());
        assertTrue(bookmarks.contains(bookmark1));
        assertTrue(bookmarks.contains(bookmark2));
    }


    @Test
    public void bookmarksCanReadById() {
        BookmarkDao dao = ctx.getBean(BookmarkDao.class);
        EntityReference bookmarkedEntity = mkAppRef();

        Bookmark bookmark1 = createBookmark(bookmarkedEntity, "test bookmark1", dao);
        assertEquals(bookmark1, dao.getById(bookmark1.id().get()));
    }


    @Test
    public void bookmarksCanDeletedById() {
        BookmarkDao dao = ctx.getBean(BookmarkDao.class);
        EntityReference bookmarkedEntity = mkAppRef();

        Bookmark bookmark = createBookmark(bookmarkedEntity, "test bookmark1", dao);
        Long bookmarkId = bookmark.id().get();
        assertEquals(bookmark, dao.getById(bookmarkId));
        assertTrue(dao.deleteById(bookmarkId));
        assertNull(dao.getById(bookmarkId));
    }


    @Test
    public void bookmarksCanBeUpdated() {
        BookmarkDao dao = ctx.getBean(BookmarkDao.class);
        EntityReference bookmarkedEntity = mkAppRef();

        Bookmark bookmark = createBookmark(bookmarkedEntity, "test bookmark1", dao);
        Long bookmarkId = bookmark.id().get();
        assertEquals(bookmark, dao.getById(bookmarkId));

        ImmutableBookmark updatedBookmark = ImmutableBookmark.copyOf(bookmark)
                .withTitle("Updated")
                .withLastUpdatedAt(DateTimeUtilities.today().atStartOfDay().plusHours(1));

        dao.update(updatedBookmark, "admin");
        assertEquals(updatedBookmark, dao.getById(bookmarkId));
    }


    @Test
    public void bookmarksAreAttachedToSpecificEntities() {
        EntityReference bookmarkedEntity = mkAppRef();
        EntityReference anotherBookmarkedEntity = mkAppRef();
        Bookmark bookmark1 = createBookmark(bookmarkedEntity, "test bookmark1", dao);
        Bookmark bookmark2 = createBookmark(anotherBookmarkedEntity, "test bookmark2", dao);

        List<Bookmark> bookmarksForFirstEntity = dao.findByReference(bookmarkedEntity);
        assertEquals(1, bookmarksForFirstEntity.size());
        assertTrue(bookmarksForFirstEntity.contains(bookmark1));

        List<Bookmark> bookmarksForSecondEntity = dao.findByReference(anotherBookmarkedEntity);
        assertEquals(1, bookmarksForSecondEntity.size());
        assertTrue(bookmarksForSecondEntity.contains(bookmark2));
    }


    // -- HELPERS ----

    private Bookmark createBookmark(EntityReference entity, String title, BookmarkDao dao) {
        return dao.create(ImmutableBookmark
                        .builder()
                        .title(title)
                        .url("https://github.com/finos/waltz")
                        .parent(entity)
                        .bookmarkKind("DOCUMENTATION")
                        .lastUpdatedBy("admin")
                        .lastUpdatedAt(DateTimeUtilities.today().atStartOfDay())
                        .build(),
                "admin");
    }


    private EntityReference mkAppRef() {
        return mkRef(EntityKind.APPLICATION, refCounter.incrementAndGet());
    }

}