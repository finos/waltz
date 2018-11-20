package com.khartec.waltz.service.taxonomy_management;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.measurable.Measurable;
import com.khartec.waltz.model.taxonomy_management.ImmutableTaxonomyChangeImpact;
import com.khartec.waltz.model.taxonomy_management.ImmutableTaxonomyChangePreview;
import com.khartec.waltz.model.taxonomy_management.TaxonomyChangeCommand;
import com.khartec.waltz.service.measurable.MeasurableService;
import com.khartec.waltz.service.measurable_rating.MeasurableRatingService;

import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;

public class TaxonomyManagementUtilities {


    public static Measurable validateMeasurable(MeasurableService measurableService,
                                                TaxonomyChangeCommand cmd) {
        long measurableId = cmd.a().id();
        Measurable measurable = measurableService.getById(measurableId);

        checkNotNull(
                measurable,
                "Cannot find measurable [%d]",
                measurableId);

        checkTrue(
                cmd.changeDomain().id() == measurable.categoryId(),
                "Measurable [%s / %d] is not in category [%d], instead it is in category [%d]",
                measurable.name(),
                measurable.id(),
                cmd.changeDomain().id(),
                measurable.categoryId());

        return measurable;
    }


    public static Set<EntityReference> findCurrentRatingMappings(MeasurableRatingService measurableRatingService,
                                                                 TaxonomyChangeCommand cmd) {
        IdSelectionOptions selectionOptions = mkOpts(cmd.a(), HierarchyQueryScope.EXACT);
        return measurableRatingService
                .findByMeasurableIdSelector(selectionOptions)
                .stream()
                .map(r -> r.entityReference())
                .collect(Collectors.toSet());
    }


    public static ImmutableTaxonomyChangePreview.Builder addToPreview(ImmutableTaxonomyChangePreview.Builder preview,
                                                                      Set<EntityReference> refs,
                                                                      Severity severity,
                                                                      String msg) {
        return preview
                .addImpacts(ImmutableTaxonomyChangeImpact.builder()
                .impactedReferences(refs)
                .description(msg)
                .severity(severity)
                .build());
    }

}
