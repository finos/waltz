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

package com.khartec.waltz.data.authoritative_source;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.authoritativesource.AuthoritativeSource;
import com.khartec.waltz.model.authoritativesource.ImmutableAuthoritativeSource;
import com.khartec.waltz.model.authoritativesource.Rating;
import com.khartec.waltz.schema.tables.records.AuthoritativeSourceRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SelectOnConditionStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.*;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.AuthoritativeSource.AUTHORITATIVE_SOURCE;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;


@Repository
public class AuthoritativeSourceDao {


    private final DSLContext dsl;

    private final RecordMapper<Record, AuthoritativeSource> authSourceMapper = r -> {
        AuthoritativeSourceRecord record = r.into(AuthoritativeSourceRecord.class);

        EntityReference parentRef = ImmutableEntityReference.builder()
                .id(record.getParentId())
                .kind(EntityKind.valueOf(record.getParentKind()))
                .build();

        EntityReference orgUnitRef = ImmutableEntityReference.builder()
                .kind(EntityKind.ORG_UNIT)
                .id(r.getValue(ORGANISATIONAL_UNIT.ID))
                .name(r.getValue(ORGANISATIONAL_UNIT.NAME))
                .build();

        EntityReference appRef = ImmutableEntityReference.builder()
                .kind(EntityKind.APPLICATION)
                .id(r.getValue(APPLICATION.ID))
                .name(r.getValue(APPLICATION.NAME))
                .build();

        return ImmutableAuthoritativeSource.builder()
                .id(record.getId())
                .parentReference(parentRef)
                .appOrgUnitReference(orgUnitRef)
                .applicationReference(appRef)
                .dataType(record.getDataType())
                .rating(Rating.valueOf(record.getRating()))
                .provenance(record.getProvenance())
                .build();
    };


    @Autowired
    public AuthoritativeSourceDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public List<AuthoritativeSource> findByEntityKind(EntityKind kind) {
        checkNotNull(kind, "kind must not be null");
        
        return baseSelect()
                .where(AUTHORITATIVE_SOURCE.PARENT_KIND.eq(kind.name()))
                .fetch(authSourceMapper);
    }


    public List<AuthoritativeSource> findByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref must not be null");
        
        
        return baseSelect()
                .where(AUTHORITATIVE_SOURCE.PARENT_KIND.eq(ref.kind().name()))
                .and(AUTHORITATIVE_SOURCE.PARENT_ID.eq(ref.id()))
                .fetch(authSourceMapper);
    }


    private SelectOnConditionStep<Record> baseSelect() {
        return dsl.select()
                .from(AUTHORITATIVE_SOURCE)
                .innerJoin(APPLICATION)
                .on(APPLICATION.ID.eq(AUTHORITATIVE_SOURCE.APPLICATION_ID))
                .innerJoin(ORGANISATIONAL_UNIT)
                .on(ORGANISATIONAL_UNIT.ID.eq(APPLICATION.ORGANISATIONAL_UNIT_ID));
    }


    public List<AuthoritativeSource> findByApplicationId(long applicationId) {
        checkTrue(applicationId > -1, "applicationId must be +ve");
        
        return baseSelect()
                .where(AUTHORITATIVE_SOURCE.APPLICATION_ID.eq(applicationId))
                .fetch(authSourceMapper);
    }


    public int update(long id, Rating rating) {
        checkTrue(id > -1, "id must be +ve");
        checkNotNull(rating, "rating must not be null");

        return dsl.update(AUTHORITATIVE_SOURCE)
                .set(AUTHORITATIVE_SOURCE.RATING, rating.name())
                .where(AUTHORITATIVE_SOURCE.ID.eq(id))
                .execute();
    }


    public int insert(EntityReference parentRef, String dataType, Long appId, Rating rating) {
        checkNotNull(parentRef, "parentRef must not be null");
        checkNotEmptyString(dataType, "dataType cannot be empty");
        checkNotNull(rating, "rating must not be null");

        return dsl.insertInto(AUTHORITATIVE_SOURCE)
                .set(AUTHORITATIVE_SOURCE.PARENT_KIND, parentRef.kind().name())
                .set(AUTHORITATIVE_SOURCE.PARENT_ID, parentRef.id())
                .set(AUTHORITATIVE_SOURCE.DATA_TYPE, dataType)
                .set(AUTHORITATIVE_SOURCE.APPLICATION_ID, appId)
                .set(AUTHORITATIVE_SOURCE.RATING, rating.name())
                .execute();
    }


    public int remove(long id) {
        return dsl.delete(AUTHORITATIVE_SOURCE)
                .where(AUTHORITATIVE_SOURCE.ID.eq(id))
                .execute();
    }


    public AuthoritativeSource getById(long id) {
        return baseSelect()
                .where(AUTHORITATIVE_SOURCE.ID.eq(id))
                .fetchOne(authSourceMapper);
    }


    public List<AuthoritativeSource> findByEntityReferences(EntityKind kind, List<Long> ids) {
        return baseSelect()
                .where(AUTHORITATIVE_SOURCE.PARENT_KIND.eq(kind.name()))
                .and(AUTHORITATIVE_SOURCE.PARENT_ID.in(ids))
                .fetch(authSourceMapper);
    }
}
