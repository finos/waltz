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

package com.khartec.waltz.data.changelog;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.schema.tables.records.ChangeLogRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.JooqUtilities.*;
import static com.khartec.waltz.schema.tables.ChangeLog.CHANGE_LOG;


@Repository
public class ChangeLogDao {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeLogDao.class);

    private final DSLContext dsl;
    private RecordMapper<? super Record, ChangeLog> mapper = r -> {
        ChangeLogRecord record = r.into(ChangeLogRecord.class);

        EntityReference parentRef = ImmutableEntityReference.builder()
                .id(record.getParentId())
                .kind(EntityKind.valueOf(record.getParentKind()))
                .build();

        return ImmutableChangeLog.builder()
                .userId(record.getUserId())
                .message(record.getMessage())
                .severity(Severity.valueOf(record.getSeverity()))
                .parentReference(parentRef)
                .createdAt(record.getCreatedAt().toLocalDateTime())
                .build();
    };


    @Autowired
    public ChangeLogDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public List<ChangeLog> findByParentReference(EntityReference ref,
                                                 Optional<Integer> limit) {
        checkNotNull(ref, "ref must not be null");

        return dsl.select()
                .from(CHANGE_LOG)
                .where(CHANGE_LOG.PARENT_ID.eq(ref.id()))
                .and(CHANGE_LOG.PARENT_KIND.eq(ref.kind().name()))
                .orderBy(CHANGE_LOG.CREATED_AT.desc())
                .limit(limit.orElse(Integer.MAX_VALUE))
                .fetch(mapper);
    }


    public List<ChangeLog> findByUser(String userName) {
        checkNotEmpty(userName, "Username cannot be empty");

        return dsl.select()
                .from(CHANGE_LOG)
                .where(CHANGE_LOG.USER_ID.equalIgnoreCase(userName))
                .orderBy(CHANGE_LOG.CREATED_AT.desc())
                .fetch(mapper);
    }


    public List<Tally<String>> getContributionLeaderBoard(int limit) {
        return makeTallyQuery(
                    dsl,
                    CHANGE_LOG,
                    CHANGE_LOG.USER_ID,
                    DSL.trueCondition())
                .orderBy(TALLY_COUNT_FIELD.desc())
                .limit(limit)
                .fetch(TO_STRING_TALLY);
    }


    public List<Tally<String>> getContributionScoresForUsers(List<String> userIds) {
        return calculateStringTallies(
                dsl,
                CHANGE_LOG,
                CHANGE_LOG.USER_ID,
                CHANGE_LOG.USER_ID.in(userIds));
    }


    public int write(ChangeLog changeLog) {
        checkNotNull(changeLog, "changeLog must not be null");

        return dsl.insertInto(CHANGE_LOG)
                .set(CHANGE_LOG.MESSAGE, changeLog.message())
                .set(CHANGE_LOG.PARENT_ID, changeLog.parentReference().id())
                .set(CHANGE_LOG.PARENT_KIND, changeLog.parentReference().kind().name())
                .set(CHANGE_LOG.USER_ID, changeLog.userId())
                .set(CHANGE_LOG.SEVERITY, changeLog.severity().name())
                .set(CHANGE_LOG.CREATED_AT, Timestamp.valueOf(changeLog.createdAt()))
                .execute();
    }


}
