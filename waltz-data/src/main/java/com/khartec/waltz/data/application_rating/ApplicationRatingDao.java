package com.khartec.waltz.data.application_rating;

import com.khartec.waltz.common.MapBuilder;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.application_rating.ApplicationRating;
import com.khartec.waltz.model.application_rating.ImmutableApplicationRating;
import com.khartec.waltz.model.capabilityrating.RagRating;
import com.khartec.waltz.schema.tables.records.ApplicationRatingRecord;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.toLocalDateTime;
import static com.khartec.waltz.schema.tables.ApplicationRating.APPLICATION_RATING;

@Repository
public class ApplicationRatingDao {

    private static final Map<EntityKind, Field<Long>> PERSPECTIVE_FIELDS_MAP = new MapBuilder<EntityKind, Field<Long>>()
            .add(EntityKind.CAPABILITY, APPLICATION_RATING.CAPABILITY_ID)
            .build();

    private static final RecordMapper<? super Record, ApplicationRating> TO_DOMAIN_MAPPER = r -> {
        ApplicationRatingRecord record = r.into(APPLICATION_RATING);

        return ImmutableApplicationRating.builder()
                .applicationId(record.getApplicationId())
                .capabilityId(record.getCapabilityId())
                .processId(record.getProcessId())
                .regionId(record.getRegionId())
                .businessLineId(record.getBusinessLineId())
                .productId(record.getProductId())
                .rating(RagRating.valueOf(record.getRating()))
                .weight(record.getWeight())
                .description(record.getDescription())
                .provenance(record.getProvenance())
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .build();
    };

    private final DSLContext dsl;


    @Autowired
    public ApplicationRatingDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public List<ApplicationRating> findRatingsById(EntityKind perspective, long applicationId) {
        checkNotNull(perspective, "perspective cannot be null");

        Field<Long> perspectiveField = PERSPECTIVE_FIELDS_MAP.get(perspective);
        checkNotNull(perspectiveField, "perspectiveField could not be determined for kind: " + perspective);

        return dsl.select()
                .from(APPLICATION_RATING)
                .where(APPLICATION_RATING.APPLICATION_ID.eq(applicationId))
                .and(perspectiveField.isNotNull())
                .fetch(TO_DOMAIN_MAPPER);
    }
}
