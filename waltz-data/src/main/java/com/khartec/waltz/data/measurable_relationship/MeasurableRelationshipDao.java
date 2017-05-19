/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

package com.khartec.waltz.data.measurable_relationship;

import com.khartec.waltz.model.measurable_relationship.ImmutableMeasurableRelationship;
import com.khartec.waltz.model.measurable_relationship.MeasurableRelationship;
import com.khartec.waltz.model.measurable_relationship.MeasurableRelationshipKind;
import com.khartec.waltz.schema.tables.records.MeasurableRelationshipRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.toLocalDateTime;
import static com.khartec.waltz.schema.tables.MeasurableRelationship.MEASURABLE_RELATIONSHIP;
import static org.jooq.impl.DSL.when;

@Repository
public class MeasurableRelationshipDao {

    private static final RecordMapper<Record, MeasurableRelationship> TO_DOMAIN_MAPPER = r -> {
        MeasurableRelationshipRecord record = r.into(MEASURABLE_RELATIONSHIP);

        return ImmutableMeasurableRelationship.builder()
                .measurableA(record.getMeasurableA())
                .measurableB(record.getMeasurableB())
                .relationshipKind(MeasurableRelationshipKind.valueOf(record.getRelationship()))
                .description(record.getDescription())
                .provenance(record.getProvenance())
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public MeasurableRelationshipDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<MeasurableRelationship> findForMeasurable(long measurableId) {
        return dsl.selectFrom(MEASURABLE_RELATIONSHIP)
                .where(MEASURABLE_RELATIONSHIP.MEASURABLE_A.eq(measurableId))
                .or(MEASURABLE_RELATIONSHIP.MEASURABLE_B.eq(measurableId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public int remove(long measurable1, long measurable2) {
        Condition condition = MEASURABLE_RELATIONSHIP.MEASURABLE_A.eq(measurable1)
                .and(MEASURABLE_RELATIONSHIP.MEASURABLE_B.eq(measurable2));

        return dsl.deleteFrom(MEASURABLE_RELATIONSHIP)
                .where(condition)
                .execute();
    }


    public boolean hasExistingRelationship(long measurableA, long measurableB) {
        Condition exists = DSL.exists(dsl.select(MEASURABLE_RELATIONSHIP.MEASURABLE_A)
                .from(MEASURABLE_RELATIONSHIP)
                .where(MEASURABLE_RELATIONSHIP.MEASURABLE_A.eq(measurableA))
                .and(MEASURABLE_RELATIONSHIP.MEASURABLE_B.eq(measurableB)));

        return dsl.select(when(exists, true).otherwise(false)).fetchOne(Record1::value1);
    }


    public boolean save(MeasurableRelationship rel) {
        MeasurableRelationshipRecord record = new MeasurableRelationshipRecord();

        record.setMeasurableA(rel.measurableA());
        record.setMeasurableB(rel.measurableB());
        record.setRelationship(rel.relationshipKind().name());
        record.setDescription(rel.description());
        record.setLastUpdatedAt(Timestamp.valueOf(rel.lastUpdatedAt()));
        record.setLastUpdatedBy(rel.lastUpdatedBy());
        record.setProvenance(rel.provenance());

        if (dsl.executeUpdate(record) == 1) {
            return true;
        } else {
            return dsl.executeInsert(record) == 1;
        }
    }
}
