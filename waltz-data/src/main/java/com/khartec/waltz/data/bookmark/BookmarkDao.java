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

package com.khartec.waltz.data.bookmark;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.bookmark.Bookmark;
import com.khartec.waltz.model.bookmark.ImmutableBookmark;
import com.khartec.waltz.schema.tables.records.BookmarkRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.*;
import static com.khartec.waltz.schema.tables.Bookmark.BOOKMARK;


@Repository
public class BookmarkDao {


    private final DSLContext dsl;

    private RecordMapper<? super Record, Bookmark> TO_DOMAIN_MAPPER = r -> {
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
                .bookmarkKind(record.getKind())
                .isPrimary(record.getIsPrimary())
                .isRequired(record.getIsRequired())
                .provenance(record.getProvenance())
                .lastUpdatedBy(record.getLastUpdatedBy())
                .lastUpdatedAt(record.getUpdatedAt().toLocalDateTime())
                .build();
    };


    @Autowired
    public BookmarkDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public List<Bookmark> findByReference(EntityReference reference) {
        return dsl.select()
                .from(BOOKMARK)
                .where(BOOKMARK.PARENT_ID.eq(reference.id()))
                .and(BOOKMARK.PARENT_KIND.eq(reference.kind().name()))
                .orderBy(BOOKMARK.IS_PRIMARY.desc(), BOOKMARK.TITLE.asc())
                .fetch(TO_DOMAIN_MAPPER);
    }


    public Bookmark getById(long bookmarkId) {
        return dsl.select()
                .from(BOOKMARK)
                .where(BOOKMARK.ID.eq(bookmarkId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    /**
     * @param id
     * @return true if bookmark deleted
     */
    public boolean deleteById(long id) {
        checkTrue(id >= 0, "id must be positive");

        return dsl.delete(BOOKMARK)
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
                .set(BOOKMARK.KIND, bookmark.bookmarkKind())
                .set(BOOKMARK.PARENT_ID, bookmark.parent().id())
                .set(BOOKMARK.PARENT_KIND, bookmark.parent().kind().name())
                .set(BOOKMARK.IS_PRIMARY, bookmark.isPrimary())
                .set(BOOKMARK.CREATED_AT, DSL.currentTimestamp())
                .set(BOOKMARK.UPDATED_AT, DSL.currentTimestamp())
                .set(BOOKMARK.LAST_UPDATED_BY, username.trim())
                .set(BOOKMARK.PROVENANCE, bookmark.provenance())
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

        int rc = dsl.update(BOOKMARK)
                .set(BOOKMARK.KIND, bookmark.bookmarkKind())
                .set(BOOKMARK.DESCRIPTION, bookmark.description().orElse(""))
                .set(BOOKMARK.URL, bookmark.url().orElse(""))
                .set(BOOKMARK.TITLE, bookmark.title().orElse(""))
                .set(BOOKMARK.IS_PRIMARY, bookmark.isPrimary())
                .set(BOOKMARK.UPDATED_AT, DSL.currentTimestamp())
                .set(BOOKMARK.LAST_UPDATED_BY, username.trim())
                .set(BOOKMARK.PROVENANCE, bookmark.provenance())
                .where(BOOKMARK.ID.eq(bookmark.id().get()))
                .execute();

        return rc == 1 ? bookmark : null;
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("BookmarkDao{");
        sb.append('}');
        return sb.toString();
    }

}
