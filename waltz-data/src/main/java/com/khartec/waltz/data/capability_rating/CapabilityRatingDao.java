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

package com.khartec.waltz.data.capability_rating;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.capabilityrating.CapabilityRating;
import com.khartec.waltz.model.capabilityrating.ImmutableCapabilityRating;
import com.khartec.waltz.model.capabilityrating.RagRating;
import com.khartec.waltz.model.capabilityrating.RatingChange;
import com.khartec.waltz.schema.tables.records.PerspectiveRatingRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.PerspectiveRating.PERSPECTIVE_RATING;


@Deprecated
@Repository
public class CapabilityRatingDao {


    private final DSLContext dsl;


    private final Field[] fields = new Field[] {
            PERSPECTIVE_RATING.PARENT_ID,
            PERSPECTIVE_RATING.PARENT_KIND,
            PERSPECTIVE_RATING.RATING,
            PERSPECTIVE_RATING.MEASURE_CODE,
            PERSPECTIVE_RATING.CAPABILITY_ID
    };

    private final RecordMapper<Record, CapabilityRating> capabilityRatingMapper =
            r -> {
                PerspectiveRatingRecord ratingRecord = r.into(PerspectiveRatingRecord.class);

                return ImmutableCapabilityRating.builder()
                        .ragRating(RagRating.valueOf(ratingRecord.getRating()))
                        .parent(ImmutableEntityReference.builder()
                                .id(ratingRecord.getParentId())
                                .kind(EntityKind.valueOf(ratingRecord.getParentKind()))
                                .build())
                        .capabilityId(ratingRecord.getCapabilityId())
                        .measurableCode(ratingRecord.getMeasureCode())
                        .build();
            };


    @Autowired
    public CapabilityRatingDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");

        this.dsl = dsl;
    }


    public List<CapabilityRating> findByParent(EntityReference parentRef) {
        return prepareSelectPart()
                .where(PERSPECTIVE_RATING.PARENT_ID.eq(parentRef.id()))
                .and(PERSPECTIVE_RATING.PARENT_KIND.eq(parentRef.kind().name()))
                .fetch(capabilityRatingMapper);
    }


    private SelectJoinStep<Record> prepareSelectPart() {
        return dsl.select(fields)
                .from(PERSPECTIVE_RATING);
    }


    public int[] update(EntityReference parent, String perspectiveCode, List<RatingChange> updates) {

        List<UpdateConditionStep<PerspectiveRatingRecord>> updateStatements = updates.stream()
                .map(u -> dsl
                        .update(PERSPECTIVE_RATING)
                        .set(PERSPECTIVE_RATING.RATING, u.current().name())
                        .where(PERSPECTIVE_RATING.MEASURE_CODE.eq(u.measurable().code()))
                        .and(PERSPECTIVE_RATING.PARENT_ID.eq(parent.id()))
                        .and(PERSPECTIVE_RATING.PARENT_KIND.eq(parent.kind().name()))
                        .and(PERSPECTIVE_RATING.CAPABILITY_ID.eq(u.capability().id()))
                        .and(PERSPECTIVE_RATING.PERSPECTIVE_CODE.eq(perspectiveCode)))
                .collect(Collectors.toList());

        return dsl.batch(updateStatements).execute();
    }


    public int[] delete(EntityReference parent, String perspectiveCode, List<RatingChange> deletes) {

        List<DeleteConditionStep<PerspectiveRatingRecord>> deleteStatements = deletes.stream()
                .map(d -> dsl.deleteFrom(PERSPECTIVE_RATING)
                        .where(PERSPECTIVE_RATING.MEASURE_CODE.eq(d.measurable().code()))
                        .and(PERSPECTIVE_RATING.PARENT_ID.eq(parent.id()))
                        .and(PERSPECTIVE_RATING.PARENT_KIND.eq(parent.kind().name()))
                        .and(PERSPECTIVE_RATING.CAPABILITY_ID.eq(d.capability().id()))
                        .and(PERSPECTIVE_RATING.PERSPECTIVE_CODE.eq(perspectiveCode)))
                .collect(Collectors.toList());

        return dsl.batch(deleteStatements).execute();
    }


    public int[] create(EntityReference parent, String perspectiveCode, List<RatingChange> changes) {

        List<Insert> insertStatements = changes.stream()
                .map(c -> dsl
                        .insertInto(PERSPECTIVE_RATING)
                        .columns(
                                PERSPECTIVE_RATING.PARENT_ID,
                                PERSPECTIVE_RATING.PARENT_KIND,
                                PERSPECTIVE_RATING.PERSPECTIVE_CODE,
                                PERSPECTIVE_RATING.MEASURE_CODE,
                                PERSPECTIVE_RATING.CAPABILITY_ID,
                                PERSPECTIVE_RATING.RATING)
                        .values(
                                parent.id(),
                                parent.kind().name(),
                                perspectiveCode,
                                c.measurable().code(),
                                c.capability().id(),
                                c.current().name()
                        ))
                .collect(Collectors.toList());

        return dsl.batch(insertStatements).execute();
    }


    public List<CapabilityRating> findByCapabilityIds(List<Long> capIds) {
        return prepareSelectPart()
                .where(PERSPECTIVE_RATING.CAPABILITY_ID.in(capIds))
                .fetch(capabilityRatingMapper);
    }


    public List<CapabilityRating> findByAppIds(Long[] appIds) {
        return prepareSelectPart()
                .where(PERSPECTIVE_RATING.PARENT_ID.in(appIds))
                .and(PERSPECTIVE_RATING.PARENT_KIND.eq(EntityKind.APPLICATION.name()))
                .fetch(capabilityRatingMapper);
    }

    public List<CapabilityRating> findByAppIdSelector(Select<Record1<Long>> appIdSelector) {
        Condition condition = PERSPECTIVE_RATING.PARENT_ID.in(appIdSelector)
                .and(PERSPECTIVE_RATING.PARENT_KIND.eq(EntityKind.APPLICATION.name()));

        return prepareSelectPart()
                .where(dsl.renderInlined(condition))
                .fetch(capabilityRatingMapper);

    }
}
