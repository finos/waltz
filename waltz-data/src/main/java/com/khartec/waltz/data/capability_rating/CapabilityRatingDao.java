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

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableCodedReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.capabilityrating.CapabilityRating;
import com.khartec.waltz.model.capabilityrating.ImmutableCapabilityRating;
import com.khartec.waltz.model.capabilityrating.RagRating;
import com.khartec.waltz.model.capabilityrating.RatingChange;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.schema.tables.records.CapabilityRecord;
import com.khartec.waltz.schema.tables.records.PerspectiveMeasurableRecord;
import com.khartec.waltz.schema.tables.records.PerspectiveRatingRecord;
import com.khartec.waltz.schema.tables.records.PerspectiveRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.model.utils.IdUtilities.toIds;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.Capability.CAPABILITY;
import static com.khartec.waltz.schema.tables.Perspective.PERSPECTIVE;
import static com.khartec.waltz.schema.tables.PerspectiveMeasurable.PERSPECTIVE_MEASURABLE;
import static com.khartec.waltz.schema.tables.PerspectiveRating.PERSPECTIVE_RATING;


@Repository
public class CapabilityRatingDao {


    private final DSLContext dsl;
    private final OrganisationalUnitDao orgUnitDao;


    private final Field[] fields = new Field[] {
            PERSPECTIVE_RATING.PARENT_ID,
            PERSPECTIVE_RATING.PARENT_KIND,
            PERSPECTIVE_RATING.RATING,
            PERSPECTIVE.CODE,
            PERSPECTIVE.NAME,
            PERSPECTIVE_MEASURABLE.CODE,
            PERSPECTIVE_MEASURABLE.NAME,
            CAPABILITY.NAME,
            CAPABILITY.ID,
    };

    private final RecordMapper<Record, CapabilityRating> capabilityRatingMapper =
            r -> {
                PerspectiveRatingRecord ratingRecord = r.into(PerspectiveRatingRecord.class);
                PerspectiveRecord perspectiveRecord = r.into(PerspectiveRecord.class);
                PerspectiveMeasurableRecord measurableRecord = r.into(PerspectiveMeasurableRecord.class);
                CapabilityRecord capabilityRecord =r.into(CapabilityRecord.class);

                return ImmutableCapabilityRating.builder()
                        .ragRating(RagRating.valueOf(ratingRecord.getRating()))
                        .parent(ImmutableEntityReference.builder()
                                .id(ratingRecord.getParentId())
                                .kind(EntityKind.valueOf(ratingRecord.getParentKind()))
                                .build())
                        .capability(ImmutableEntityReference.builder()
                                .id(capabilityRecord.getId())
                                .name(capabilityRecord.getName())
                                .kind(EntityKind.CAPABILITY)
                                .build())
                        .perspective(ImmutableCodedReference.builder()
                                .name(perspectiveRecord.getName())
                                .code(perspectiveRecord.getCode())
                                .build())
                        .measurable(ImmutableCodedReference.builder()
                                .name(measurableRecord.getName())
                                .code(measurableRecord.getCode())
                                .build())
                        .build();
            };


    @Autowired
    public CapabilityRatingDao(DSLContext dsl, OrganisationalUnitDao orgUnitDao) {
        checkNotNull(dsl, "dsl must not be null");
        checkNotNull(orgUnitDao, "orgUnitDao must not be null");
        
        this.dsl = dsl;
        this.orgUnitDao = orgUnitDao;
    }


    public List<CapabilityRating> findByParent(EntityReference parentRef) {
        return prepareSelectPart()
                .where(PERSPECTIVE_RATING.PARENT_ID.eq(parentRef.id()))
                .and(PERSPECTIVE_RATING.PARENT_KIND.eq(parentRef.kind().name()))
                .fetch(capabilityRatingMapper);
    }


    public List<CapabilityRating> findByParentAndPerspective(EntityReference parentRef, String perspectiveCode) {
        return prepareSelectPart()
                .where(PERSPECTIVE_RATING.PARENT_ID.eq(parentRef.id()))
                .and(PERSPECTIVE_RATING.PARENT_KIND.eq(parentRef.kind().name()))
                .and(PERSPECTIVE.CODE.eq(perspectiveCode))
                .fetch(capabilityRatingMapper);
    }


    public List<CapabilityRating> findByCapability(long capabilityId) {
        return findByCapabilityIds(newArrayList(capabilityId));
    }


    public List<CapabilityRating> findByOrganisationalUnit(long orgUnitId) {
        return prepareSelectPart()
                .innerJoin(APPLICATION)
                .on(PERSPECTIVE_RATING.PARENT_KIND.eq(EntityKind.APPLICATION.name())
                        .and(PERSPECTIVE_RATING.PARENT_ID.eq(APPLICATION.ID)))
                .where(APPLICATION.ORGANISATIONAL_UNIT_ID.eq(orgUnitId))
                .fetch(capabilityRatingMapper);
    }


    public List<CapabilityRating> findByOrganisationalUnitTree(long orgUnitId) {

        List<OrganisationalUnit> orgUnitTree = orgUnitDao.findDescendants(orgUnitId);
        List<Long> orgUnitIds = toIds(orgUnitTree);

        return prepareSelectPart()
                .innerJoin(APPLICATION)
                .on(PERSPECTIVE_RATING.PARENT_KIND.eq(EntityKind.APPLICATION.name())
                        .and(PERSPECTIVE_RATING.PARENT_ID.eq(APPLICATION.ID)))
                .where(APPLICATION.ORGANISATIONAL_UNIT_ID.in(orgUnitIds))
                .fetch(capabilityRatingMapper);
    }


    private SelectOnConditionStep<Record>  prepareSelectPart() {
        return dsl.select(fields)
                .from(PERSPECTIVE_RATING)
                .innerJoin(PERSPECTIVE_MEASURABLE)
                .on(PERSPECTIVE_RATING.MEASURE_CODE.eq(PERSPECTIVE_MEASURABLE.CODE))
                .innerJoin(PERSPECTIVE)
                .on(PERSPECTIVE_MEASURABLE.PERSPECTIVE_CODE.eq(PERSPECTIVE.CODE))
                .innerJoin(CAPABILITY)
                .on(CAPABILITY.ID.eq(PERSPECTIVE_RATING.CAPABILITY_ID));
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
}
