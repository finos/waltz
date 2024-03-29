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

package org.finos.waltz.service.bookmark;

import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.data.EntityReferenceNameResolver;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.bookmark.BookmarkDao;
import org.finos.waltz.data.bookmark.BookmarkIdSelectorFactory;
import org.finos.waltz.model.*;
import org.finos.waltz.model.bookmark.Bookmark;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;


@Service
public class BookmarkService {

    private final BookmarkDao bookmarkDao;
    private final ChangeLogService changeLogService;
    private final BookmarkIdSelectorFactory bookmarkIdSelectorFactory = new BookmarkIdSelectorFactory();
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();
    private final EntityReferenceNameResolver entityReferenceNameResolver;


    @Autowired
    public BookmarkService(BookmarkDao bookmarkDao,
                           ChangeLogService changeLogService,
                           EntityReferenceNameResolver entityReferenceNameResolver) {
        checkNotNull(bookmarkDao, "bookmarkDao must not be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        this.bookmarkDao = bookmarkDao;
        this.changeLogService = changeLogService;
        this.entityReferenceNameResolver = entityReferenceNameResolver;
    }


    public List<Bookmark> findByReference(EntityReference reference) {
        return bookmarkDao.findByReference(reference);
    }


    public Bookmark create(Bookmark bookmark, String username) {
        logChange("Added", bookmark, username, Operation.ADD);
        return bookmarkDao.create(bookmark, username);
    }


    public Bookmark update(Bookmark bookmark, String username) {
        logChange("Updated", bookmark, username, Operation.UPDATE);
        return bookmarkDao.update(bookmark, username);
    }


    /**
     * @param bookmark
     * @return true if bookmark deleted
     */
    public boolean deleteById(Bookmark bookmark, String username) {
        logChange("Removed", bookmark, username, Operation.REMOVE);
        return bookmarkDao.deleteById(bookmark.id().get());
    }


    public Bookmark getById(long bookmarkId) {
        return bookmarkDao.getById(bookmarkId);
    }


    private void logChange(String verb, Bookmark bookmark, String username, Operation operation) {
        changeLogService.write(ImmutableChangeLog.builder()
                .message(String.format("%s bookmark: %s / %s to %s / %s",
                        verb,
                        bookmark.title().orElse("?"),
                        bookmark.bookmarkKind(),
                        bookmark.parent().kind().prettyName(),
                        getEntityName(bookmark.parent())
                ))
                .parentReference(bookmark.parent())
                .userId(username)
                .severity(Severity.INFORMATION)
                .childKind(EntityKind.BOOKMARK)
                .operation(operation)
                .build());
    }


    public Set<Bookmark> findByBookmarkIdSelector(IdSelectionOptions selectionOptions) {
        return bookmarkDao.findByBookmarkIdSelector(bookmarkIdSelectorFactory.apply(selectionOptions));
    }


    public int deleteByBookmarkIdSelector(IdSelectionOptions selectionOptions) {
        GenericSelector selector = genericSelectorFactory.apply(selectionOptions);
        return bookmarkDao
                .deleteByParentSelector(selector);
    }


    private String getEntityName(EntityReference entityReference) {
        return (entityReference.name().isPresent()
                        ? entityReference
                        : entityReferenceNameResolver.resolve(entityReference).orElse(entityReference))
                .name()
                .orElse("");
    }

}
