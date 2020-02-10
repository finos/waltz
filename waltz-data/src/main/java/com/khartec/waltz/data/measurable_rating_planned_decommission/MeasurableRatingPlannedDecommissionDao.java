package com.khartec.waltz.data.measurable_rating_planned_decommission;


import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.measurable_rating_planned_decommission.ImmutableMeasurableRatingPlannedDecommission;
import com.khartec.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommission;
import com.khartec.waltz.schema.tables.records.MeasurableRatingPlannedDecommissionRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.toLocalDateTime;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.tables.MeasurableRatingPlannedDecommission.MEASURABLE_RATING_PLANNED_DECOMMISSION;

@Repository
public class MeasurableRatingPlannedDecommissionDao {

    private final DSLContext dsl;


    @Autowired
    MeasurableRatingPlannedDecommissionDao(DSLContext dsl){
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public static final RecordMapper<? super Record, MeasurableRatingPlannedDecommission> TO_DOMAIN_MAPPER = record -> {

        MeasurableRatingPlannedDecommissionRecord r = record.into(MEASURABLE_RATING_PLANNED_DECOMMISSION);

        return ImmutableMeasurableRatingPlannedDecommission.builder()
                .id(r.getId())
                .entityReference(mkRef(EntityKind.valueOf(r.getEntityKind()), r.getEntityId()))
                .measurableId(r.getMeasurableId())
                .plannedDecommissionDate(toLocalDateTime(r.getPlannedDecommissionDate()))
                .createdAt(toLocalDateTime(r.getCreatedAt()))
                .createdBy(r.getCreatedBy())
                .lastUpdatedAt(toLocalDateTime(r.getUpdatedAt()))
                .lastUpdatedBy(r.getUpdatedBy())
                .build();
    };


    public MeasurableRatingPlannedDecommission getById(Long id){
        return dsl
                .selectFrom(MEASURABLE_RATING_PLANNED_DECOMMISSION)
                .where(MEASURABLE_RATING_PLANNED_DECOMMISSION.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<MeasurableRatingPlannedDecommission> fetchByEntityRef(EntityReference ref){
        return dsl
                .selectFrom(MEASURABLE_RATING_PLANNED_DECOMMISSION)
                .where(MEASURABLE_RATING_PLANNED_DECOMMISSION.ENTITY_ID.eq(ref.id())
                        .and(MEASURABLE_RATING_PLANNED_DECOMMISSION.ENTITY_KIND.eq(ref.kind().name())))
                .fetch(TO_DOMAIN_MAPPER);
    }
}
