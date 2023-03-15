package org.finos.waltz.service.bulk_upload.assessment_strategy;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.model.BulkChangeStatistics;
import org.finos.waltz.model.DiffResult;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.ImmutableBulkChangeStatistics;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.schema.tables.records.AssessmentRatingRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.DeleteConditionStep;
import org.jooq.UpdateConditionStep;
import org.jooq.lambda.tuple.Tuple3;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.data.JooqUtilities.summarizeResults;
import static org.finos.waltz.model.DiffResult.mkDiff;
import static org.finos.waltz.schema.Tables.ASSESSMENT_RATING;

public class ReplaceStrategy implements AssessmentStrategy {


    public BulkChangeStatistics apply(DSLContext tx,
                                      AssessmentDefinition definition,
                                      Set<Tuple3<Long, Long, String>> requiredRatings,
                                      Set<Tuple3<Long, Long, String>> existingRatings,
                                      String username) {

        DiffResult<Tuple3<Long, Long, String>> diffResult = mkDiff(existingRatings, requiredRatings, Tuple3::limit2, Tuple3::equals);

        Timestamp now = DateTimeUtilities.nowUtcTimestamp();

        Collection<Tuple3<Long, Long, String>> toAdd = diffResult.otherOnly();
        Collection<Tuple3<Long, Long, String>> toRemove = diffResult.waltzOnly();
        Collection<Tuple3<Long, Long, String>> toUpdate = diffResult.intersection(); //comment and timestamp

        Set<AssessmentRatingRecord> recordsToInsert = map(toAdd, d -> AssessmentStrategy.mkAssessmentRatingRecord(tx, definition.id().get(), d, now, username));
        Set<UpdateConditionStep<AssessmentRatingRecord>> updateStatements = map(toUpdate, d -> mkUpdateAssessmentStmt(tx, definition.id().get(), d, now, username));
        Set<DeleteConditionStep<AssessmentRatingRecord>> removalStatements = map(toRemove, d -> mkRemovalStmt(tx, definition.id().get(), d));

        int[] added = tx.batchInsert(recordsToInsert).execute();
        int[] updated = tx.batch(updateStatements).execute();
        int[] removed = tx.batch(removalStatements).execute();

        return ImmutableBulkChangeStatistics.builder()
                .addedCount(summarizeResults(added))
                .updatedCount(summarizeResults(updated))
                .removedCount(summarizeResults(removed))
                .build();
    }


    private DeleteConditionStep<AssessmentRatingRecord> mkRemovalStmt(DSLContext tx, Long defnId, Tuple3<Long, Long, String> assessmentInfo) {
        Condition sameAssessmentRatingCond = mkSameAssessmentRatingCond(defnId, assessmentInfo.v1, assessmentInfo.v2);

        return tx
                .deleteFrom(ASSESSMENT_RATING)
                .where(sameAssessmentRatingCond);
    }

    private UpdateConditionStep<AssessmentRatingRecord> mkUpdateAssessmentStmt(DSLContext tx,
                                                                               Long defnId,
                                                                               Tuple3<Long, Long, String> assessmentInfo,
                                                                               Timestamp now,
                                                                               String username) {

        Condition sameAssessmentRatingCond = mkSameAssessmentRatingCond(defnId, assessmentInfo.v1, assessmentInfo.v2);

        return tx
                .update(ASSESSMENT_RATING)
                .set(ASSESSMENT_RATING.DESCRIPTION, assessmentInfo.v3)
                .set(ASSESSMENT_RATING.LAST_UPDATED_BY, username)
                .set(ASSESSMENT_RATING.LAST_UPDATED_AT, now)
                .where(sameAssessmentRatingCond);
    }

    private Condition mkSameAssessmentRatingCond(Long defnId, Long entityId, Long ratingId) {
        return ASSESSMENT_RATING.ASSESSMENT_DEFINITION_ID.eq(defnId)
                .and(ASSESSMENT_RATING.ENTITY_ID.eq(entityId)
                        .and(ASSESSMENT_RATING.ENTITY_KIND.eq(EntityKind.LEGAL_ENTITY_RELATIONSHIP.name())
                                .and(ASSESSMENT_RATING.RATING_ID.eq(ratingId))));
    }

}
