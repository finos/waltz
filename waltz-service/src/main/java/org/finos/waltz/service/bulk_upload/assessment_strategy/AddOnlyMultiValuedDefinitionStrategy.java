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
import org.jooq.UpdateConditionStep;
import org.jooq.lambda.tuple.Tuple3;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

import static java.lang.String.format;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.data.JooqUtilities.summarizeResults;
import static org.finos.waltz.schema.Tables.ASSESSMENT_RATING;

public class AddOnlyMultiValuedDefinitionStrategy implements AssessmentStrategy {


    @Override
    public BulkChangeStatistics apply(DSLContext tx,
                                      AssessmentDefinition definition,
                                      Set<Tuple3<Long, Long, String>> requiredRatings,
                                      Set<Tuple3<Long, Long, String>> existingRatings,
                                      String username) {

        Timestamp now = DateTimeUtilities.nowUtcTimestamp();

        DiffResult<Tuple3<Long, Long, String>> diffResult = DiffResult.mkDiff(
                existingRatings,
                requiredRatings,
                Tuple3::limit2, // key is entity id and rating id as MVA
                Tuple3::equals); // diff based on everything including comment

        Collection<Tuple3<Long, Long, String>> toAdd = diffResult.otherOnly();
        Collection<Tuple3<Long, Long, String>> toUpdate = diffResult.intersection(); //rating, comment and timestamp

        Set<AssessmentRatingRecord> recordsToInsert = map(toAdd, d -> AssessmentStrategy.mkAssessmentRatingRecord(tx, definition.id().get(), d, now, username));
        Set<UpdateConditionStep<AssessmentRatingRecord>> updateStatements = map(
                toUpdate,
                d -> mkUpdateMultiValuedAssessmentStmt(
                        tx,
                        definition.id().get(),
                        d.v1,
                        d.v2,
                        d.v3,
                        now,
                        username));

        int[] added = tx.batchInsert(recordsToInsert).execute();
        int[] updated = tx.batch(updateStatements).execute();

        return ImmutableBulkChangeStatistics.builder()
                .addedCount(summarizeResults(added))
                .updatedCount(summarizeResults(updated))
                .removedCount(0)
                .build();
    }

    private UpdateConditionStep<AssessmentRatingRecord> mkUpdateMultiValuedAssessmentStmt(DSLContext tx,
                                                                                          Long defnId,
                                                                                          Long entityId,
                                                                                          Long ratingId,
                                                                                          String comment,
                                                                                          Timestamp now,
                                                                                          String username) {

        Condition sameAssessmentRatingCond = ASSESSMENT_RATING.ASSESSMENT_DEFINITION_ID.eq(defnId)
                .and(ASSESSMENT_RATING.ENTITY_ID.eq(entityId)
                        .and(ASSESSMENT_RATING.ENTITY_KIND.eq(EntityKind.LEGAL_ENTITY_RELATIONSHIP.name())
                                .and(ASSESSMENT_RATING.RATING_ID.eq(ratingId))));
        return tx
                .update(ASSESSMENT_RATING)
                .set(ASSESSMENT_RATING.DESCRIPTION, comment)
                .set(ASSESSMENT_RATING.LAST_UPDATED_BY, username)
                .set(ASSESSMENT_RATING.LAST_UPDATED_AT, now)
                .where(sameAssessmentRatingCond);
    }

}
