/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.service.taxonomy_management;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.HierarchyQueryScope;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.finos.waltz.model.taxonomy_management.ImmutableTaxonomyChangeImpact;
import org.finos.waltz.model.taxonomy_management.ImmutableTaxonomyChangePreview;
import org.finos.waltz.model.taxonomy_management.TaxonomyChangeCommand;
import org.finos.waltz.service.measurable.MeasurableService;
import org.finos.waltz.service.measurable_rating.MeasurableRatingService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.*;
import static org.finos.waltz.common.SetUtilities.fromCollection;
import static org.finos.waltz.common.SetUtilities.minus;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;

public class TaxonomyManagementUtilities {


    public static Measurable validatePrimaryMeasurable(MeasurableService measurableService,
                                                       TaxonomyChangeCommand cmd) {
        long measurableId = cmd.primaryReference().id();
        long categoryId = cmd.changeDomain().id();
        return validateMeasurableInCategory(measurableService, measurableId, categoryId);
    }

    public static void validateMeasurablesInCategory(MeasurableService measurableService,
                                                        List<Long> ids,
                                                        long categoryId) {
        List<Measurable> allMeasurablesInCategory = measurableService.findByCategoryId(categoryId);

        checkEmpty(
            minus(
                fromCollection(ids),
                SetUtilities.map(
                    allMeasurablesInCategory,
                    m -> m.id().get())),
            format(
                "Not all measurables: (%s) in category: %d",
                ids.toString(),
                categoryId));
    }


    public static Measurable validateMeasurableInCategory(MeasurableService measurableService,
                                                long measurableId,
                                                long categoryId) {
        Measurable measurable = measurableService.getById(measurableId);

        checkNotNull(
                measurable,
                "Cannot find measurable [%d]",
                measurableId);

        checkTrue(
                categoryId == measurable.categoryId(),
                "Measurable [%s / %d] is not in category [%d], instead it is in category [%d]",
                measurable.name(),
                measurable.id(),
                categoryId,
                measurable.categoryId());

        return measurable;
    }


    public static Set<EntityReference> findCurrentRatingMappings(MeasurableRatingService measurableRatingService,
                                                                 TaxonomyChangeCommand cmd) {
        IdSelectionOptions selectionOptions = mkOpts(cmd.primaryReference(), HierarchyQueryScope.EXACT);
        return measurableRatingService
                .findByMeasurableIdSelector(selectionOptions)
                .stream()
                .map(MeasurableRating::entityReference)
                .collect(Collectors.toSet());
    }


    /**
     * Optionally add an impact to the given preview and return it.
     * Whether to add the impact is determined by the presence of references.
     *
     * @param preview The preview builder to update
     * @param refs Set of references, if empty no impact will be added to the preview
     * @param severity Severity of the impact
     * @param msg Description of the impact
     * @return The preview builder for convenience
     */
    public static ImmutableTaxonomyChangePreview.Builder addToPreview(ImmutableTaxonomyChangePreview.Builder preview,
                                                                      Set<EntityReference> refs,
                                                                      Severity severity,
                                                                      String msg) {
        return refs.isEmpty()
            ? preview
            : preview
                .addImpacts(ImmutableTaxonomyChangeImpact.builder()
                .impactedReferences(refs)
                .description(msg)
                .severity(severity)
                .build());
    }


    public static String getNameParam(TaxonomyChangeCommand cmd) {
        return cmd.param("name");
    }


    public static String getDescriptionParam(TaxonomyChangeCommand cmd) {
        return cmd.param("description");
    }


    public static String getExternalIdParam(TaxonomyChangeCommand cmd) {
        return cmd.param("externalId");
    }


    public static boolean getConcreteParam(TaxonomyChangeCommand cmd, boolean dflt) {
        return cmd.paramAsBoolean("concrete", dflt);
    }
}
