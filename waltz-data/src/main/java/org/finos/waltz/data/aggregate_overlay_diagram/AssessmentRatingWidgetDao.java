package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.common.MapUtilities;
import org.finos.waltz.data.rating_scheme.RatingSchemeDAO;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.AssessmentRatingCount;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.AssessmentRatingsWidgetDatum;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.ImmutableAssessmentRatingCount;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.ImmutableAssessmentRatingsWidgetDatum;
import org.finos.waltz.model.utils.IdUtilities;
import org.finos.waltz.schema.tables.AssessmentRating;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.*;

@Repository
public class AssessmentRatingWidgetDao {

    private static final AssessmentRating ar = AssessmentRating.ASSESSMENT_RATING;

    private final RatingSchemeDAO ratingSchemeDAO;
    private final DSLContext dsl;


    @Autowired
    public AssessmentRatingWidgetDao(DSLContext dsl,
                                     RatingSchemeDAO ratingSchemeDAO) {
        this.dsl = dsl;
        this.ratingSchemeDAO = ratingSchemeDAO;
    }


    public Set<AssessmentRatingsWidgetDatum> findWidgetData(long diagramId,
                                                            EntityKind aggregatedEntityKind,
                                                            Long assessmentId,
                                                            Select<Record1<Long>> inScopeEntityIdSelector) {

        Select<Record2<String, Long>> cellExtIdWithEntityIdSelector = mkOverlayEntityCellAggregateEntitySelector(dsl, diagramId, aggregatedEntityKind);

        if (cellExtIdWithEntityIdSelector == null) {
            // no cell mapping data so short circuit and give no results
            return Collections.emptySet();
        }

        Map<String, Set<Long>> cellExtIdsToEntityIdsMap = fetchAndGroupEntityIdsByCellId(dsl, cellExtIdWithEntityIdSelector);

        Set<Long> diagramEntityIds = calcExactEntityIdsOnDiagram(
                dsl,
                cellExtIdsToEntityIdsMap,
                inScopeEntityIdSelector);

        Map<Long, org.finos.waltz.model.rating.RatingSchemeItem> itemsById = IdUtilities.indexById(ratingSchemeDAO.findRatingSchemeItemsForAssessmentDefinition(assessmentId));

        if (itemsById.isEmpty()) {
            return Collections.emptySet();
        }

        Map<Long, Long> entityToRatingMap = dsl
                .select(ar.RATING_ID, ar.ENTITY_ID)
                .from(ar)
                .where(ar.ASSESSMENT_DEFINITION_ID.eq(assessmentId))
                .and(ar.ENTITY_ID.in(diagramEntityIds))
                .and(ar.ENTITY_KIND.eq(aggregatedEntityKind.name()))
                .fetchMap(ar.ENTITY_ID, ar.RATING_ID);

        return cellExtIdsToEntityIdsMap
                .entrySet()
                .stream()
                .map(e -> {
                    String cellExtId = e.getKey();
                    Set<Long> entityIds = e.getValue();

                    Map<Long, AtomicInteger> counts = MapUtilities.newHashMap();
                    entityIds.forEach(id -> {
                        Long rating = entityToRatingMap.get(id);
                        if (rating == null) {
                            return;
                        }
                        AtomicInteger count = counts.getOrDefault(rating, new AtomicInteger());
                        count.incrementAndGet();
                        counts.put(rating, count);
                    });

                    Set<AssessmentRatingCount> countsWithRating = map(
                            counts.entrySet(),
                            kv -> ImmutableAssessmentRatingCount.builder()
                                    .rating(itemsById.get(kv.getKey()))
                                    .count(kv.getValue().get())
                                    .build());

                    return ImmutableAssessmentRatingsWidgetDatum.builder()
                            .cellExternalId(cellExtId)
                            .counts(countsWithRating)
                            .build();
                })
                .collect(toSet());
    }
}
