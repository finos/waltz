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

package org.finos.waltz.service.taxonomy_management.processors;

import org.finos.waltz.service.bookmark.BookmarkService;
import org.finos.waltz.service.entity_relationship.EntityRelationshipService;
import org.finos.waltz.service.flow_diagram.FlowDiagramEntityService;
import org.finos.waltz.service.involvement.InvolvementService;
import org.finos.waltz.service.measurable.MeasurableService;
import org.finos.waltz.service.measurable_rating.MeasurableRatingService;
import org.finos.waltz.service.taxonomy_management.TaxonomyCommandProcessor;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.model.*;
import org.finos.waltz.model.bookmark.Bookmark;
import org.finos.waltz.model.flow_diagram.FlowDiagramEntity;
import org.finos.waltz.model.involvement.Involvement;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.finos.waltz.model.taxonomy_management.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.finos.waltz.service.taxonomy_management.TaxonomyManagementUtilities.addToPreview;
import static org.finos.waltz.service.taxonomy_management.TaxonomyManagementUtilities.validatePrimaryMeasurable;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.SetUtilities.*;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;

@Service
public class RemoveMeasurableCommandProcessor implements TaxonomyCommandProcessor {

    private final MeasurableService measurableService;
    private final TaxonomyManagementHelper taxonomyManagementHelper;

    @Autowired
    public RemoveMeasurableCommandProcessor(MeasurableService measurableService,
                                            TaxonomyManagementHelper taxonomyManagementHelper) {
        checkNotNull(measurableService, "measurableService cannot be null");

        this.measurableService = measurableService;
        this.taxonomyManagementHelper = taxonomyManagementHelper;
    }


    @Override
    public Set<TaxonomyChangeType> supportedTypes() {
        return asSet(TaxonomyChangeType.REMOVE);
    }


    @Override
    public EntityKind domain() {
        return EntityKind.MEASURABLE_CATEGORY;
    }


    public TaxonomyChangePreview preview(TaxonomyChangeCommand cmd) {
        doBasicValidation(cmd);
        Measurable primaryMeasurable = validatePrimaryMeasurable(measurableService, cmd);

        ImmutableTaxonomyChangePreview.Builder preview = ImmutableTaxonomyChangePreview
                .builder()
                .command(ImmutableTaxonomyChangeCommand
                        .copyOf(cmd)
                        .withPrimaryReference(primaryMeasurable.entityReference()));

        IdSelectionOptions selectionOptions = mkOpts(cmd.primaryReference(), HierarchyQueryScope.CHILDREN);

        taxonomyManagementHelper.previewChildNodeRemovals(preview, selectionOptions);
        taxonomyManagementHelper.previewAppMappingRemovals(preview, selectionOptions);
        taxonomyManagementHelper.previewBookmarkRemovals(preview, selectionOptions);
        taxonomyManagementHelper.previewInvolvementRemovals(preview, selectionOptions);
        taxonomyManagementHelper.previewFlowDiagramRemovals(preview, selectionOptions);
        taxonomyManagementHelper.previewEntityRelationships(preview, selectionOptions);

        // TODO: entitySvgDiagrams, roadmapScenarios

        return preview.build();
    }

    public TaxonomyChangeCommand apply(TaxonomyChangeCommand cmd, String userId) {
        doBasicValidation(cmd);
        Measurable measurable = validatePrimaryMeasurable(measurableService, cmd);

        IdSelectionOptions selectionOptions = mkOpts(cmd.primaryReference(), HierarchyQueryScope.CHILDREN);

        taxonomyManagementHelper.removeBookmarks(selectionOptions);
        taxonomyManagementHelper.removeInvolvements(selectionOptions);
        taxonomyManagementHelper.removeAppMappings(selectionOptions);
        taxonomyManagementHelper.removeMeasurables(selectionOptions);
        taxonomyManagementHelper.removeFlowDiagrams(selectionOptions);
        taxonomyManagementHelper.removeEntityRelationshipsDiagrams(selectionOptions);
        taxonomyManagementHelper.removeAssessments(EntityKind.MEASURABLE, selectionOptions);
        taxonomyManagementHelper.removeNamedNotes(selectionOptions);


        String message = String.format("Measurable %s has been removed", measurable.name());
        Optional<Long> measurableId = measurable.parentId().isPresent()
                ? measurable.parentId()
                : measurable.id();
        measurableId.ifPresent(id -> measurableService.writeAuditMessage(id, userId, message));

        // TODO: entitySvgDiagrams, roadmapScenarios

        return ImmutableTaxonomyChangeCommand
                .copyOf(cmd)
                .withStatus(TaxonomyChangeLifecycleStatus.EXECUTED)
                .withLastUpdatedBy(userId)
                .withLastUpdatedAt(DateTimeUtilities.nowUtc());
    }



}
