package org.finos.waltz.service.bulk_upload.assessment_strategy;

import org.finos.waltz.model.BulkChangeStatistics;
import org.finos.waltz.model.Cardinality;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.bulk_upload.BulkUpdateMode;
import org.finos.waltz.schema.tables.records.AssessmentRatingRecord;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;

import java.sql.Timestamp;
import java.util.Set;

import static java.lang.String.format;
import static org.finos.waltz.schema.Tables.ASSESSMENT_RATING;
import static org.jooq.lambda.tuple.Tuple.tuple;

public interface AssessmentStrategy {

    static AssessmentStrategy determineStrategy(BulkUpdateMode updateMode, Cardinality cardinality) {


        switch (updateMode) {
            case ADD_ONLY:
                return determineAddOnlyStrategy(cardinality);
            case REPLACE:
                return new ReplaceStrategy();
            default:
                throw new IllegalStateException(format("Cannot determine update strategy for mode %s", updateMode.name()));
        }
    }

    static AssessmentStrategy determineAddOnlyStrategy(Cardinality cardinality) {

        switch (cardinality) {
            case ZERO_ONE:
                return new AddOnlySingleValuedDefinitionStrategy();
            case ZERO_MANY:
                return new AddOnlyMultiValuedDefinitionStrategy();
            default:
                throw new IllegalStateException(format("Cannot determine add only update strategy for cardinality: %s", cardinality.name()));
        }
    }

    BulkChangeStatistics apply(DSLContext tx,
                               AssessmentDefinition definition,
                               Set<Tuple3<Long, Long, String>> requiredRatings,
                               Set<Tuple3<Long, Long, String>> existingRatings,
                               String username);


    static AssessmentRatingRecord mkAssessmentRatingRecord(DSLContext tx,
                                                           Long defnId,
                                                           Tuple3<Long, Long, String> assessmentInfo,
                                                           Timestamp now,
                                                           String username) {

        AssessmentRatingRecord r = tx.newRecord(ASSESSMENT_RATING);
        r.setAssessmentDefinitionId(defnId);
        r.setEntityId(assessmentInfo.v1);
        r.setEntityKind(EntityKind.LEGAL_ENTITY_RELATIONSHIP.name());
        r.setRatingId(assessmentInfo.v2);
        r.setDescription(assessmentInfo.v3);
        r.setLastUpdatedBy(username);
        r.setLastUpdatedAt(now);
        r.setProvenance("waltz");
        r.setIsReadonly(false);

        return r;
    }
}
