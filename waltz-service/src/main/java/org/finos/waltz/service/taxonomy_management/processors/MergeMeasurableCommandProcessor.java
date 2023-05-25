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

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.HierarchyQueryScope;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.taxonomy_management.*;
import org.finos.waltz.service.measurable.MeasurableService;
import org.finos.waltz.service.taxonomy_management.TaxonomyCommandProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.finos.waltz.service.taxonomy_management.TaxonomyManagementUtilities.validateMeasurableInCategory;

@Service
public class MergeMeasurableCommandProcessor implements TaxonomyCommandProcessor {

    private final MeasurableService measurableService;
    private final TaxonomyManagementHelper taxonomyManagementHelper;

    @Autowired
    public MergeMeasurableCommandProcessor(MeasurableService measurableService,
                                           TaxonomyManagementHelper taxonomyManagementHelper) {
        checkNotNull(measurableService, "measurableService cannot be null");
        checkNotNull(taxonomyManagementHelper, "taxonomyManagementHelper cannot be null");
        this.taxonomyManagementHelper = taxonomyManagementHelper;
        this.measurableService = measurableService;
    }


    @Override
    public Set<TaxonomyChangeType> supportedTypes() {
        return SetUtilities.asSet(TaxonomyChangeType.MERGE);
    }


    @Override
    public EntityKind domain() {
        return EntityKind.MEASURABLE_CATEGORY;
    }


    public TaxonomyChangePreview preview(TaxonomyChangeCommand cmd) {
        Measurable primaryRef = validate(cmd);

        ImmutableTaxonomyChangePreview.Builder previewBuilder = ImmutableTaxonomyChangePreview
                .builder()
                .command(ImmutableTaxonomyChangeCommand
                        .copyOf(cmd)
                        .withPrimaryReference(primaryRef.entityReference()));

        IdSelectionOptions opts = IdSelectionOptions.mkOpts(cmd.primaryReference(), HierarchyQueryScope.EXACT);

        taxonomyManagementHelper.previewBookmarkRemovals(previewBuilder, opts);
        taxonomyManagementHelper.previewInvolvementRemovals(previewBuilder, opts);
        taxonomyManagementHelper.previewEntityNamedNoteRemovals(previewBuilder, opts);
        taxonomyManagementHelper.previewAssessmentRemovals(previewBuilder, opts);
        taxonomyManagementHelper.previewFlowDiagramRemovals(previewBuilder, opts);

        ImmutableTaxonomyChangePreview preview = previewBuilder.build();

        return preview;
    }


    public TaxonomyChangeCommand apply(TaxonomyChangeCommand cmd, String userId) {
        Measurable measurableToMerge = validate(cmd);
        Long target = getTarget(cmd);

        if (target == null) {
            throw new IllegalArgumentException("Cannot perform a merge where the target has not been identified");
        }

        String targetName = getTargetName(cmd);
        IdSelectionOptions selectionOptions = mkOpts(measurableToMerge.entityReference(), HierarchyQueryScope.EXACT); // children are migrated, do not want to delete their data

        taxonomyManagementHelper.migrateMeasurable(selectionOptions, target, userId);

        int removedBookmarks = taxonomyManagementHelper.removeBookmarks(selectionOptions);
        int removedInvolvements = taxonomyManagementHelper.removeInvolvements(selectionOptions);
        int removedNotes = taxonomyManagementHelper.removeNamedNotes(selectionOptions);
        int removedDiagrams = taxonomyManagementHelper.removeFlowDiagrams(selectionOptions);
        int removedAssessments = taxonomyManagementHelper.removeAssessments(EntityKind.MEASURABLE, selectionOptions);

        String message = format("Merged measurable: %s [%d] into target: %s [%d] - Removed: %d bookmarks; %d involvements; %d notes; %d flow diagram relationships; %d assessments",
                measurableToMerge.name(),
                measurableToMerge.id().get(),
                targetName,
                target,
                removedBookmarks,
                removedInvolvements,
                removedNotes,
                removedDiagrams,
                removedAssessments);

        measurableService.writeAuditMessage(target, userId, message);

        return ImmutableTaxonomyChangeCommand
                .copyOf(cmd)
                .withLastUpdatedAt(DateTimeUtilities.nowUtc())
                .withLastUpdatedBy(userId)
                .withStatus(TaxonomyChangeLifecycleStatus.EXECUTED);
    }


    private Measurable validate(TaxonomyChangeCommand cmd) {
        doBasicValidation(cmd);
        long categoryId = cmd.changeDomain().id();
        Measurable m = validateMeasurableInCategory(measurableService, cmd.primaryReference().id(), categoryId);
        Long targetId = getTarget(cmd);
        if (targetId != null) {
            validateMeasurableInCategory(measurableService, targetId, categoryId);
        }
        return m;
    }


    private Long getTarget(TaxonomyChangeCommand cmd) {
        return cmd.paramAsLong("targetId", null);
    }


    private String getTargetName(TaxonomyChangeCommand cmd) {
        return cmd.param("targetName");
    }

}
