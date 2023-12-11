package org.finos.waltz.service.measurable_rating;

import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.allocation.Allocation;
import org.finos.waltz.model.allocation_scheme.AllocationScheme;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.measurable_rating.ImmutableMeasurableRatingView;
import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.finos.waltz.model.measurable_rating.MeasurableRatingView;
import org.finos.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommission;
import org.finos.waltz.model.measurable_rating_replacement.MeasurableRatingReplacement;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.service.allocation.AllocationService;
import org.finos.waltz.service.allocation_schemes.AllocationSchemeService;
import org.finos.waltz.service.measurable.MeasurableService;
import org.finos.waltz.service.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommissionService;
import org.finos.waltz.service.measurable_rating_replacement.MeasurableRatingReplacementService;
import org.finos.waltz.service.rating_scheme.RatingSchemeService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.finos.waltz.common.MapUtilities.indexBy;

@Service
public class MeasurableRatingViewService {

    private final MeasurableRatingService measurableRatingService;
    private final MeasurableService measurableService;
    private final MeasurableRatingPlannedDecommissionService measurableRatingPlannedDecommissionService;
    private final MeasurableRatingReplacementService measurableRatingReplacementService;
    private final RatingSchemeService ratingSchemeService;

    private final AllocationService allocationService;
    private final AllocationSchemeService allocationSchemeService;

    public MeasurableRatingViewService(MeasurableRatingService measurableRatingService,
                                       MeasurableService measurableService,
                                       MeasurableRatingPlannedDecommissionService measurableRatingPlannedDecommissionService,
                                       MeasurableRatingReplacementService measurableRatingReplacementService,
                                       RatingSchemeService ratingSchemeService,
                                       AllocationService allocationService,
                                       AllocationSchemeService allocationSchemeService){

        this.measurableRatingService = measurableRatingService;
        this.measurableService = measurableService;
        this.measurableRatingPlannedDecommissionService = measurableRatingPlannedDecommissionService;
        this.measurableRatingReplacementService = measurableRatingReplacementService;
        this.ratingSchemeService = ratingSchemeService;
        this.allocationService = allocationService;
        this.allocationSchemeService = allocationSchemeService;
    }

    public MeasurableRatingView getViewById(long id) {

        MeasurableRating measurableRating = measurableRatingService.getById(id);

        Measurable measurable = measurableService.getById(measurableRating.measurableId());

        if (measurable == null) {

            return ImmutableMeasurableRatingView.builder()
                    .measurableRating(measurableRating)
                    .measurable(null)
                    .rating(null)
                    .decommission(null)
                    .replacements(emptyList())
                    .build();
        } else {

            EntityReference measurableRef = measurable.entityReference();

            List<RatingSchemeItem> ratingSchemeItems = ratingSchemeService.findRatingSchemeItemsForEntityAndCategory(measurableRating.entityReference(), measurable.categoryId());

            Map<String, RatingSchemeItem> itemsByCode = indexBy(ratingSchemeItems, RatingSchemeItem::rating);
            RatingSchemeItem rating = itemsByCode.get(String.valueOf(measurableRating.rating()));

            MeasurableRatingPlannedDecommission decomm = measurableRatingPlannedDecommissionService.getByMeasurableRatingId(id);

            Set<MeasurableRatingReplacement> replacementApps = decomm == null
                    ? Collections.emptySet()
                    : measurableRatingReplacementService.getByDecommId(decomm.id());

            List<AllocationScheme> schemes = allocationSchemeService.findByCategoryId(measurable.categoryId());

            Set<Allocation> allocations = allocationService.findByMeasurableRatingId(id);

            return ImmutableMeasurableRatingView.builder()
                    .measurableRating(measurableRating)
                    .measurable(measurableRef)
                    .rating(rating)
                    .decommission(decomm)
                    .replacements(replacementApps)
                    .allocations(allocations)
                    .allocationSchemes(schemes)
                    .build();
        }

    }
}
