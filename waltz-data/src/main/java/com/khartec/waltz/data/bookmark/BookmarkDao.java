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

package com.khartec.waltz.data.bookmark;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.bookmark.Bookmark;
import com.khartec.waltz.model.bookmark.BookmarkKind;
import com.khartec.waltz.model.bookmark.ImmutableBookmark;
import com.khartec.waltz.schema.tables.records.BookmarkRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.*;
import static com.khartec.waltz.schema.tables.Bookmark.BOOKMARK;


@Repository
public class BookmarkDao {


    private final DSLContext dsl;

    private RecordMapper<? super Record, Bookmark> bookmarkMapper = r -> {
        BookmarkRecord record = r.into(BookmarkRecord.class);

        EntityReference parentRef = ImmutableEntityReference.builder()
                .id(record.getParentId())
                .kind(EntityKind.valueOf(record.getParentKind()))
                .build();

        return ImmutableBookmark.builder()
                .id(record.getId())
                .parent(parentRef)
                .description(record.getDescription())
                .title(record.getTitle())
                .url(record.getUrl())
                .kind(BookmarkKind.valueOf(record.getKind()))
                .isPrimary(record.getIsPrimary())
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
                .fetch(bookmarkMapper);
    }


    public Bookmark getById(long bookmarkId) {
        return dsl.select()
                .from(BOOKMARK)
                .where(BOOKMARK.ID.eq(bookmarkId))
                .fetchOne(bookmarkMapper);
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


    public Bookmark create(Bookmark bookmark) {
        checkNotNull(bookmark, "bookmark must not be null");

        BookmarkRecord insertedRecord = dsl.insertInto(BOOKMARK)
                .set(BOOKMARK.DESCRIPTION, bookmark.description().orElse(""))
                .set(BOOKMARK.URL, bookmark.url().orElse(""))
                .set(BOOKMARK.TITLE, bookmark.title().orElse(""))
                .set(BOOKMARK.KIND, bookmark.kind().name())
                .set(BOOKMARK.PARENT_ID, bookmark.parent().id())
                .set(BOOKMARK.PARENT_KIND, bookmark.parent().kind().name())
                .set(BOOKMARK.IS_PRIMARY, bookmark.isPrimary())
                .set(BOOKMARK.CREATED_AT, DSL.currentTimestamp())
                .set(BOOKMARK.UPDATED_AT, DSL.currentTimestamp())
                .returning(BOOKMARK.ID)
                .fetchOne();


        return ImmutableBookmark.builder()
                .from(bookmark)
                .id(insertedRecord.getId())
                .build();
    }


    public Bookmark update(Bookmark bookmark) {
        checkNotNull(bookmark, "bookmark must not be null");
        checkOptionalIsPresent(bookmark.id(), "bookmark id is required for an update");

        int rc = dsl.update(BOOKMARK)
                .set(BOOKMARK.KIND, bookmark.kind().name())
                .set(BOOKMARK.DESCRIPTION, bookmark.description().orElse(""))
                .set(BOOKMARK.URL, bookmark.url().orElse(""))
                .set(BOOKMARK.TITLE, bookmark.title().orElse(""))
                .set(BOOKMARK.IS_PRIMARY, bookmark.isPrimary())
                .set(BOOKMARK.UPDATED_AT, DSL.currentTimestamp())
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
