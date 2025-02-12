package org.finos.waltz.service.assessment_rating;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.AssessmentRating;
import org.finos.waltz.schema.tables.LogicalFlow;
import org.finos.waltz.schema.tables.PhysicalFlow;
import org.finos.waltz.schema.tables.records.AssessmentDefinitionRecord;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.lambda.tuple.Tuple2;

import static java.lang.String.format;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class RipplerUtils {

    private static final PhysicalFlow pf = Tables.PHYSICAL_FLOW;
    private static final AssessmentRating ar = Tables.ASSESSMENT_RATING;
    private static final LogicalFlow lf = Tables.LOGICAL_FLOW;

    public static Select<Record4<Long, Long, Long, String>> getTargetAndRatingProvider(DSLContext tx, Tuple2<EntityKind, EntityKind> kinds, AssessmentDefinitionRecord from, EntityReference parentEntitytRef) {
        if (kinds.equals(tuple(EntityKind.PHYSICAL_FLOW, EntityKind.LOGICAL_DATA_FLOW))) {
            // PHYSICAL_FLOW -> LOGICAL
            SelectConditionStep<Record1<Long>> logicalFlowIds = tx.select(pf.LOGICAL_FLOW_ID)
                    .from(pf)
                    .where(pf.ID.eq(parentEntitytRef.id()));

            SelectConditionStep<Record1<Long>> physicalFlowIds = tx.select(pf.ID)
                    .from(pf)
                    .where(pf.LOGICAL_FLOW_ID.in(logicalFlowIds));

            return tx.select(lf.ID, ar.RATING_ID, pf.ID, pf.NAME)
                            .from(pf)
                            .innerJoin(lf).on(lf.ID.eq(pf.LOGICAL_FLOW_ID))
                                .and(pf.ID.in(physicalFlowIds))
                            .leftJoin(ar).on(ar.ENTITY_ID.eq(pf.ID))
                                .and(ar.ENTITY_KIND.eq(from.getEntityKind()))
                                .and(ar.ASSESSMENT_DEFINITION_ID.eq(from.getId()))
                            .where(pf.IS_REMOVED.isFalse())
                            .and(pf.ENTITY_LIFECYCLE_STATUS.ne(EntityLifecycleStatus.REMOVED.name()));
        }  else {
            throw new UnsupportedOperationException(format(
                    "Cannot ripple assessment from kind: %s to kind: %s",
                    kinds.v1,
                    kinds.v2));
        }
    }
}
