/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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

package com.khartec.waltz.data.assessment_rating;


import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.assessment_rating.AssessmentRating;
import com.khartec.waltz.model.assessment_rating.ImmutableAssessmentRating;
import com.khartec.waltz.model.assessment_rating.RemoveAssessmentRatingCommand;
import com.khartec.waltz.model.assessment_rating.SaveAssessmentRatingCommand;
import com.khartec.waltz.schema.tables.records.AssessmentRatingRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.toLocalDateTime;
import static com.khartec.waltz.common.StringUtilities.mkSafe;
import static com.khartec.waltz.schema.tables.AssessmentRating.ASSESSMENT_RATING;

@Repository
public class AssessmentRatingDao {

    private static final RecordMapper<? super Record, AssessmentRating> TO_DOMAIN_MAPPER = r -> {
        AssessmentRatingRecord record = r.into(ASSESSMENT_RATING);
        return ImmutableAssessmentRating.builder()
                .entityReference(EntityReference.mkRef(EntityKind.valueOf(record.getEntityKind()), record.getEntityId()))
                .assessmentDefinitionId(record.getAssessmentDefinitionId())
                .ratingId(record.getRatingId())
                .description(mkSafe(record.getDescription()))
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .provenance(record.getProvenance())
                .build();
    };


    private final Function<SaveAssessmentRatingCommand, AssessmentRatingRecord> TO_RECORD_MAPPER = command -> {
        AssessmentRatingRecord record = new AssessmentRatingRecord();
        record.setEntityId(command.entityReference().id());
        record.setEntityKind(command.entityReference().kind().name());
        record.setAssessmentDefinitionId(command.assessmentDefinitionId());
        record.setRatingId(command.ratingId());
        record.setDescription(command.description());
        record.setLastUpdatedAt(Timestamp.valueOf(command.lastUpdatedAt()));
        record.setLastUpdatedBy(command.lastUpdatedBy());
        record.setProvenance(command.provenance());
        return record;
    };


    private final DSLContext dsl;


    @Autowired
    public AssessmentRatingDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public List<AssessmentRating> findForEntity(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return dsl.selectFrom(ASSESSMENT_RATING)
                .where(ASSESSMENT_RATING.ENTITY_KIND.eq(ref.kind().name()))
                .and(ASSESSMENT_RATING.ENTITY_ID.eq(ref.id()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public boolean update(SaveAssessmentRatingCommand command) {
        checkNotNull(command, "command cannot be null");
        AssessmentRatingRecord record = TO_RECORD_MAPPER.apply(command);
        return dsl.executeUpdate(record) == 1;
    }


    public boolean create(SaveAssessmentRatingCommand command) {
        checkNotNull(command, "command cannot be null");
        AssessmentRatingRecord record = TO_RECORD_MAPPER.apply(command);
        return dsl.executeInsert(record) == 1;
    }


    public boolean remove(RemoveAssessmentRatingCommand rating) {
        return dsl.deleteFrom(ASSESSMENT_RATING)
                .where(ASSESSMENT_RATING.ENTITY_KIND.eq(rating.entityReference().kind().name()))
                .and(ASSESSMENT_RATING.ENTITY_ID.eq(rating.entityReference().id()))
                .and(ASSESSMENT_RATING.ASSESSMENT_DEFINITION_ID.eq(rating.assessmentDefinitionId()))
                .execute() == 1;
    }

}
