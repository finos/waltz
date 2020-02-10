package com.khartec.waltz.data.measurable_rating_replacement;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.measurable_rating_replacement.ImmutableMeasurableRatingReplacement;
import com.khartec.waltz.model.measurable_rating_replacement.MeasurableRatingReplacement;
import com.khartec.waltz.schema.tables.records.MeasurableRatingReplacementRecord;
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
import static com.khartec.waltz.schema.tables.MeasurableRatingReplacement.MEASURABLE_RATING_REPLACEMENT;

@Repository
public class MeasurableRatingReplacementDao {


    private final DSLContext dsl;


    @Autowired
    MeasurableRatingReplacementDao(DSLContext dsl){
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public static final RecordMapper<? super Record, MeasurableRatingReplacement> TO_DOMAIN_MAPPER =  record -> {

        MeasurableRatingReplacementRecord r = record.into(MEASURABLE_RATING_REPLACEMENT);

            return ImmutableMeasurableRatingReplacement.builder()
                    .id(r.getId())
                    .entityReference(mkRef(EntityKind.valueOf(r.getEntityKind()), r.getEntityId()))
                    .decommissionId(r.getDecommissionId())
                    .plannedCommissionDate(toLocalDateTime(r.getPlannedCommissionDate()))
                    .createdAt(toLocalDateTime(r.getCreatedAt()))
                    .createdBy(r.getCreatedBy())
                    .lastUpdatedAt(toLocalDateTime(r.getUpdatedAt()))
                    .lastUpdatedBy(r.getUpdatedBy())
                    .build();
    };


    public MeasurableRatingReplacement getById(Long id){
        return dsl
                .selectFrom(MEASURABLE_RATING_REPLACEMENT)
                .where(MEASURABLE_RATING_REPLACEMENT.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public MeasurableRatingReplacement getByDecommissionId(Long decommissionId){
        return dsl
                .selectFrom(MEASURABLE_RATING_REPLACEMENT)
                .where(MEASURABLE_RATING_REPLACEMENT.DECOMMISSION_ID.eq(decommissionId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }

    public List<MeasurableRatingReplacement> fetchByEntityRef(EntityReference ref){
        return dsl
                .select()
                .from(MEASURABLE_RATING_REPLACEMENT)
                .innerJoin(MEASURABLE_RATING_PLANNED_DECOMMISSION).on(MEASURABLE_RATING_PLANNED_DECOMMISSION.ID.eq(MEASURABLE_RATING_REPLACEMENT.DECOMMISSION_ID))
                .where(MEASURABLE_RATING_PLANNED_DECOMMISSION.ENTITY_ID.eq(ref.id())
                        .and(MEASURABLE_RATING_PLANNED_DECOMMISSION.ENTITY_KIND.eq(ref.kind().name())))
                .fetch(TO_DOMAIN_MAPPER);
    }

}
