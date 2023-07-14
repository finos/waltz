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

package org.finos.waltz.data.bookmark;

import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.model.bookmark.Bookmark;
import org.finos.waltz.model.bookmark.BookmarkKindValue;
import org.finos.waltz.model.bookmark.ImmutableBookmark;
import org.finos.waltz.schema.tables.records.BookmarkRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotEmpty;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.Checks.checkOptionalIsPresent;
import static org.finos.waltz.common.Checks.checkTrue;
import static org.finos.waltz.schema.tables.Bookmark.BOOKMARK;


@Repository
public class BookmarkDao {


    private final DSLContext dsl;

    private final RecordMapper<? super Record, Bookmark> TO_DOMAIN_MAPPER = r -> {
        BookmarkRecord record = r.into(BookmarkRecord.class);

        EntityReference parentRef = ImmutableEntityReference.builder()
                .id(record.getParentId())
                .kind(EntityKind.valueOf(record.getParentKind()))
                .build();

        return ImmutableBookmark.builder()
                .id(record.getId())
                .parent(parentRef)
                .description(Optional.ofNullable(record.getDescription()))
                .title(Optional.ofNullable(record.getTitle()))
                .url(Optional.ofNullable(record.getUrl()))
                .bookmarkKind(BookmarkKindValue.of(record.getKind()))
                .isPrimary(record.getIsPrimary())
                .isRequired(record.getIsRequired())
                .provenance(record.getProvenance())
                .lastUpdatedBy(record.getLastUpdatedBy())
                .lastUpdatedAt(record.getUpdatedAt().toLocalDateTime())
                .isRestricted(record.getIsRestricted())
                .build();
    };


    @Autowired
    public BookmarkDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public List<Bookmark> findByReference(EntityReference reference) {
        return dsl
                .select(BOOKMARK.fields())
                .from(BOOKMARK)
                .where(BOOKMARK.PARENT_ID.eq(reference.id()))
                .and(BOOKMARK.PARENT_KIND.eq(reference.kind().name()))
                .orderBy(BOOKMARK.IS_PRIMARY.desc(), BOOKMARK.TITLE.asc())
                .fetch(TO_DOMAIN_MAPPER);
    }


    public Bookmark getById(long bookmarkId) {
        return dsl
                .select(BOOKMARK.fields())
                .from(BOOKMARK)
                .where(BOOKMARK.ID.eq(bookmarkId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    /**
     * @param id bookmark identifier of item to be deleted
     * @return true if bookmark deleted
     */
    public boolean deleteById(long id) {
        checkTrue(id >= 0, "id must be positive");

        return dsl
                .delete(BOOKMARK)
                .where(BOOKMARK.ID.eq(id))
                .execute() == 1;
    }


    public Bookmark create(Bookmark bookmark, String username) {
        checkNotNull(bookmark, "bookmark must not be null");
        checkNotEmpty(username, "username cannot be empty");

        BookmarkRecord insertedRecord = dsl.insertInto(BOOKMARK)
                .set(BOOKMARK.DESCRIPTION, bookmark.description().orElse(null))
                .set(BOOKMARK.URL, bookmark.url().orElse(null))
                .set(BOOKMARK.TITLE, bookmark.title().orElse(null))
                .set(BOOKMARK.KIND, bookmark.bookmarkKind().value())
                .set(BOOKMARK.PARENT_ID, bookmark.parent().id())
                .set(BOOKMARK.PARENT_KIND, bookmark.parent().kind().name())
                .set(BOOKMARK.IS_PRIMARY, bookmark.isPrimary())
                .set(BOOKMARK.CREATED_AT, DSL.currentTimestamp())
                .set(BOOKMARK.UPDATED_AT, Timestamp.valueOf(bookmark.lastUpdatedAt()))
                .set(BOOKMARK.LAST_UPDATED_BY, username.trim())
                .set(BOOKMARK.PROVENANCE, bookmark.provenance())
                .set(BOOKMARK.IS_RESTRICTED, bookmark.isRestricted())
                .returning(BOOKMARK.ID)
                .fetchOne();

        return ImmutableBookmark.builder()
                .from(bookmark)
                .id(insertedRecord.getId())
                .build();
    }


    public Bookmark update(Bookmark bookmark, String username) {
        checkNotNull(bookmark, "bookmark must not be null");
        checkOptionalIsPresent(bookmark.id(), "bookmark id is required for an update");
        checkNotEmpty(username, "username cannot be empty");

        int rc = dsl
                .update(BOOKMARK)
                .set(BOOKMARK.KIND, bookmark.bookmarkKind().value())
                .set(BOOKMARK.DESCRIPTION, bookmark.description().orElse(null))
                .set(BOOKMARK.URL, bookmark.url().orElse(""))
                .set(BOOKMARK.TITLE, bookmark.title().orElse(""))
                .set(BOOKMARK.IS_PRIMARY, bookmark.isPrimary())
                .set(BOOKMARK.UPDATED_AT, Timestamp.valueOf(bookmark.lastUpdatedAt()))
                .set(BOOKMARK.LAST_UPDATED_BY, username.trim())
                .set(BOOKMARK.PROVENANCE, bookmark.provenance())
                .set(BOOKMARK.IS_RESTRICTED, bookmark.isRestricted())
                .where(BOOKMARK.ID.eq(bookmark.id().get()))
                .execute();

        return rc == 1 ? bookmark : null;
    }


    @Override
    public String toString() {
        return "BookmarkDao{}";
    }


    /**
     * Retrieves a collection of bookmarks via a passed in selector.  Missing bookmarks are silently
     * ignored therefore the result.size() &lt;= the number of ids returned by the selector.
     *
     * @param selector A sub-query that returns the set of bookmark ids that need to be retrieved
     * @return A collection of bookmarks corresponding to the selector.
     */
    public Set<Bookmark> findByBookmarkIdSelector(Select<Record1<Long>> selector) {
        return dsl
                .select(BOOKMARK.fields())
                .from(BOOKMARK)
                .where(BOOKMARK.ID.in(selector))
                .fetchSet(TO_DOMAIN_MAPPER);
    }


    /**
     * Bulk removes bookmarks via a passed in parent-ref selector sub-query.
     * Removed bookmarks are _deleted_ from the database.
     * @param parentRefSelector sub-query which returns a set of parent-refs associated to bookmark to be deleted
     * @return count of bookmarks removed
     */
    public int deleteByParentSelector(GenericSelector parentRefSelector) {
        return dsl
                .deleteFrom(BOOKMARK)
                .where(BOOKMARK.PARENT_ID.in(parentRefSelector.selector()))
                .and(BOOKMARK.PARENT_KIND.eq(parentRefSelector.kind().name()))
                .execute();
    }

}
