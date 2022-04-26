package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.common.MapUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.rating_scheme.RatingSchemeDAO;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.overlay_diagram.AssessmentRatingCount;
import org.finos.waltz.model.overlay_diagram.AssessmentRatingsWidgetDatum;
import org.finos.waltz.model.overlay_diagram.ImmutableAssessmentRatingCount;
import org.finos.waltz.model.overlay_diagram.ImmutableAssessmentRatingsWidgetDatum;
import org.finos.waltz.model.utils.IdUtilities;
import org.finos.waltz.schema.tables.AssessmentRating;
import org.finos.waltz.schema.tables.RatingSchemeItem;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toSet;

@Repository
public class AppAssessmentWidgetDao extends AggregateOverlayDiagramDao{

    private static final RatingSchemeItem rsi = RatingSchemeItem.RATING_SCHEME_ITEM;
    private static final AssessmentRating ar = AssessmentRating.ASSESSMENT_RATING;
    private final RatingSchemeDAO ratingSchemeDAO;


    @Autowired
    public AppAssessmentWidgetDao(DSLContext dsl,
                                  RatingSchemeDAO ratingSchemeDAO) {
        super(dsl);
        this.ratingSchemeDAO = ratingSchemeDAO;
    }


    public Set<AssessmentRatingsWidgetDatum> findWidgetData(long diagramId,
                                                            Long assessmentId,
                                                            Select<Record1<Long>> inScopeApplicationSelector) {

        Select<Record2<String, Long>> cellExtIdWithAppIdSelector = mkOverlayEntityCellApplicationSelector(diagramId);

        if (cellExtIdWithAppIdSelector == null) {
            // no cell mapping data so short circuit and give no results
            return Collections.emptySet();
        }

        Map<String, Set<Long>> cellExtIdsToAppIdsMap = fetchAndGroupAppIdsByCellId(cellExtIdWithAppIdSelector);

        Select<Record1<Long>> diagramApplicationIdSelector = mkAppIdSelectorForDiagram(
                cellExtIdWithAppIdSelector,
                inScopeApplicationSelector);

        Map<Long, org.finos.waltz.model.rating.RatingSchemeItem> itemsById = IdUtilities.indexById(ratingSchemeDAO.findRatingSchemeItemsForAssessmentDefinition(assessmentId));

        if (itemsById.isEmpty()) {
            return Collections.emptySet();
        }

        Map<Long, Long> appToRatingMap = dsl
                .select(ar.RATING_ID, ar.ENTITY_ID)
                .from(ar)
                .where(ar.ASSESSMENT_DEFINITION_ID.eq(assessmentId))
                .and(ar.ENTITY_ID.in(diagramApplicationIdSelector))
                .and(ar.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .fetchMap(ar.ENTITY_ID, ar.RATING_ID);

        return cellExtIdsToAppIdsMap
                .entrySet()
                .stream()
                .map(e -> {
                    String cellExtId = e.getKey();
                    Set<Long> appIds = e.getValue();

                    Map<Long, AtomicInteger> counts = MapUtilities.newHashMap();
                    appIds.forEach(id -> {
                            Long rating = appToRatingMap.get(id);
                            if (rating == null) {
                                return;
                            }
                            AtomicInteger count = counts.getOrDefault(rating, new AtomicInteger());
                            count.incrementAndGet();
                            counts.put(rating, count);
                        });

                    Set<AssessmentRatingCount> countsWithRating = SetUtilities.map(
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
