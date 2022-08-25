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

package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.HierarchyQueryScope;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.bookmark.Bookmark;
import org.finos.waltz.model.bookmark.BookmarkKindValue;
import org.finos.waltz.model.bookmark.ImmutableBookmark;
import org.finos.waltz.service.bookmark.BookmarkService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;
import static junit.framework.TestCase.assertTrue;
import static org.finos.waltz.common.CollectionUtilities.first;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.finos.waltz.test_common.helpers.NameHelper.mkUserId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


public class BookmarkServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private BookmarkService svc;


    @Test
    public void bookmarkCanBeCreated() {
        EntityReference bookmarkedEntity = mkAppRef();
        Bookmark bookmark = createBookmark(bookmarkedEntity, "test bookmark");
        List<Bookmark> bookmarks = svc.findByReference(bookmarkedEntity);
        assertEquals(1, bookmarks.size());
        assertEquals(bookmark, first(bookmarks));
    }


    @Test
    public void bookmarksCanBeCreated() {
        EntityReference bookmarkedEntity = mkAppRef();

        Bookmark bookmark1 = createBookmark(bookmarkedEntity, "test bookmark1");
        Bookmark bookmark2 = createBookmark(bookmarkedEntity, "test bookmark2");

        List<Bookmark> bookmarks = svc.findByReference(bookmarkedEntity);
        assertEquals(2, bookmarks.size());
        assertTrue(bookmarks.contains(bookmark1));
        assertTrue(bookmarks.contains(bookmark2));
    }


    @Test
    public void bookmarksCanReadById() {
        EntityReference bookmarkedEntity = mkAppRef();

        Bookmark bookmark1 = createBookmark(bookmarkedEntity, "test bookmark1");
        assertEquals(bookmark1, svc.getById(bookmark1.id().get()));
    }


    @Test
    public void bookmarksCanDeletedById() {
        EntityReference bookmarkedEntity = mkAppRef();

        Bookmark bookmark = createBookmark(bookmarkedEntity, "test bookmark1");
        Long bookmarkId = bookmark.id().get();
        assertEquals(bookmark, svc.getById(bookmarkId));
        assertTrue(svc.deleteById(bookmark, mkUserId()));
        assertNull(svc.getById(bookmarkId));
    }


    @Test
    public void bookmarksCanBeUpdated() {
        EntityReference bookmarkedEntity = mkAppRef();

        Bookmark bookmark = createBookmark(bookmarkedEntity, "test bookmark1");
        Long bookmarkId = bookmark.id().get();
        assertEquals(bookmark, svc.getById(bookmarkId));

        ImmutableBookmark updatedBookmark = ImmutableBookmark.copyOf(bookmark)
                .withTitle("Updated")
                .withLastUpdatedAt(DateTimeUtilities.today().atStartOfDay().plusHours(1));

        svc.update(updatedBookmark, "admin");
        assertEquals(updatedBookmark, svc.getById(bookmarkId));
    }


    @Test
    public void bookmarksAreAttachedToSpecificEntities() {
        EntityReference bookmarkedEntity = mkAppRef();
        EntityReference anotherBookmarkedEntity = mkAppRef();
        Bookmark bookmark1 = createBookmark(bookmarkedEntity, "test bookmark1");
        Bookmark bookmark2 = createBookmark(anotherBookmarkedEntity, "test bookmark2");

        List<Bookmark> bookmarksForFirstEntity = svc.findByReference(bookmarkedEntity);
        assertEquals(1, bookmarksForFirstEntity.size());
        assertTrue(bookmarksForFirstEntity.contains(bookmark1));

        List<Bookmark> bookmarksForSecondEntity = svc.findByReference(anotherBookmarkedEntity);
        assertEquals(1, bookmarksForSecondEntity.size());
        assertTrue(bookmarksForSecondEntity.contains(bookmark2));
    }


    @Test
    public void bookmarksCanBeFoundBySelector() {
        EntityReference appRef1 = mkAppRef();
        EntityReference appRef2 = mkAppRef();
        createBookmark(appRef1, "a");
        createBookmark(appRef1, "b");
        createBookmark(appRef2, "c");
        Set<Bookmark> matches = svc.findByBookmarkIdSelector(mkOpts(appRef1, HierarchyQueryScope.EXACT));
        assertEquals(2, matches.size());
        assertEquals(asSet("a", "b"), SetUtilities.map(matches, m -> m.title().get()));
    }


    @Test
    public void bookmarksCanBeRemovedBySelector() {
        EntityReference appRef1 = mkAppRef();
        EntityReference appRef2 = mkAppRef();
        IdSelectionOptions a1_opts = mkOpts(appRef1, HierarchyQueryScope.EXACT);
        IdSelectionOptions a2_opts = mkOpts(appRef2, HierarchyQueryScope.EXACT);

        createBookmark(appRef1, "a");
        createBookmark(appRef1, "b");
        createBookmark(appRef2, "c");

        int numRemoved = svc.deleteByBookmarkIdSelector(a1_opts);
        assertEquals(2, numRemoved, "both app1 bookmarks should have been removed");
        assertEquals(emptySet(), svc.findByBookmarkIdSelector(a1_opts), "no app1 bookmarks should be left");
        assertEquals(1, svc.findByBookmarkIdSelector(a2_opts).size(), "the app2 bookmark should remain");
    }


    // -- HELPERS ----

    private Bookmark createBookmark(EntityReference entity, String title) {
        return svc.create(ImmutableBookmark
                        .builder()
                        .title(title)
                        .url("https://github.com/finos/waltz")
                        .parent(entity)
                        .bookmarkKind(BookmarkKindValue.of("DOCUMENTATION"))
                        .lastUpdatedBy("admin")
                        .lastUpdatedAt(DateTimeUtilities.today().atStartOfDay())
                        .build(),
                "admin");
    }


    private EntityReference mkAppRef() {
        return mkRef(EntityKind.APPLICATION, counter.incrementAndGet());
    }

}