package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.common.MapUtilities;
import org.finos.waltz.data.rating_scheme.RatingSchemeDAO;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.AssessmentRatingCount;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.AssessmentRatingsWidgetDatum;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.ImmutableAssessmentRatingCount;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.ImmutableAssessmentRatingsWidgetDatum;
import org.finos.waltz.schema.tables.AssessmentRating;
import org.jooq.*;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.*;
import static org.finos.waltz.model.utils.IdUtilities.indexById;

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
                                                            Select<Record1<Long>> inScopeEntityIdSelector,
                                                            Optional<LocalDate> targetStateDate) {

        Set<Tuple2<String, EntityReference>> cellWithBackingEntities = loadExpandedCellMappingsForDiagram(dsl, diagramId);

        Map<String, Set<Long>> cellExtIdsToAggregatedEntities = loadCellExtIdToAggregatedEntities(
                dsl,
                cellWithBackingEntities,
                aggregatedEntityKind,
                inScopeEntityIdSelector,
                targetStateDate);

        Set<Long> diagramEntityIds = cellExtIdsToAggregatedEntities.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(toSet());

        Map<Long, org.finos.waltz.model.rating.RatingSchemeItem> itemsById = indexById(ratingSchemeDAO.findRatingSchemeItemsForAssessmentDefinition(assessmentId));

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

        return cellExtIdsToAggregatedEntities
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

                    if (counts.isEmpty()) {
                        return null;
                    }

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
                .filter(Objects::nonNull)
                .collect(toSet());
    }
}
