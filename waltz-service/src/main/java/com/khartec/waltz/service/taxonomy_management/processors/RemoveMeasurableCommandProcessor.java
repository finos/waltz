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

package com.khartec.waltz.service.taxonomy_management.processors;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.bookmark.Bookmark;
import com.khartec.waltz.model.flow_diagram.FlowDiagramEntity;
import com.khartec.waltz.model.involvement.Involvement;
import com.khartec.waltz.model.measurable.Measurable;
import com.khartec.waltz.model.measurable_rating.MeasurableRating;
import com.khartec.waltz.model.taxonomy_management.*;
import com.khartec.waltz.service.bookmark.BookmarkService;
import com.khartec.waltz.service.entity_relationship.EntityRelationshipService;
import com.khartec.waltz.service.flow_diagram.FlowDiagramEntityService;
import com.khartec.waltz.service.involvement.InvolvementService;
import com.khartec.waltz.service.measurable.MeasurableService;
import com.khartec.waltz.service.measurable_rating.MeasurableRatingService;
import com.khartec.waltz.service.taxonomy_management.TaxonomyCommandProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.SetUtilities.*;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static com.khartec.waltz.service.taxonomy_management.TaxonomyManagementUtilities.addToPreview;
import static com.khartec.waltz.service.taxonomy_management.TaxonomyManagementUtilities.validatePrimaryMeasurable;

@Service
public class RemoveMeasurableCommandProcessor implements TaxonomyCommandProcessor {

    private final BookmarkService bookmarkService;
    private final EntityRelationshipService entityRelationshipService;
    private final FlowDiagramEntityService flowDiagramEntityService;
    private final InvolvementService involvementService;
    private final MeasurableRatingService measurableRatingService;
    private final MeasurableService measurableService;


    @Autowired
    public RemoveMeasurableCommandProcessor(BookmarkService bookmarkService,
                                            EntityRelationshipService entityRelationshipService,
                                            FlowDiagramEntityService flowDiagramEntityService,
                                            InvolvementService involvementService,
                                            MeasurableRatingService measurableRatingService,
                                            MeasurableService measurableService) {
        checkNotNull(bookmarkService, "bookmarkService cannot be null");
        checkNotNull(entityRelationshipService, "entityRelationshipService cannot be null");
        checkNotNull(flowDiagramEntityService, "flowDiagramEntityService cannot be null");
        checkNotNull(involvementService, "involvementService cannot be null");
        checkNotNull(measurableRatingService, "measurableRatingService cannot be null");
        checkNotNull(measurableService, "measurableService cannot be null");

        this.bookmarkService = bookmarkService;
        this.entityRelationshipService = entityRelationshipService;
        this.flowDiagramEntityService = flowDiagramEntityService;
        this.involvementService = involvementService;
        this.measurableRatingService = measurableRatingService;
        this.measurableService = measurableService;
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

        previewChildNodeRemovals(preview, selectionOptions);
        previewAppMappingRemovals(preview, selectionOptions);
        previewBookmarkRemovals(preview, selectionOptions);
        previewInvolvementRemovals(preview, selectionOptions);
        previewFlowDiagramRemovals(preview, selectionOptions);
        previewEntityRelationships(preview, selectionOptions);

        // TODO: entitySvgDiagrams, roadmapScenarios

        return preview.build();
    }


    private void previewEntityRelationships(ImmutableTaxonomyChangePreview.Builder preview,
                                            IdSelectionOptions options) {
        Set<EntityReference> refs = entityRelationshipService
                .findForGenericEntitySelector(options)
                .stream()
                .flatMap(rel -> Stream.of(rel.a(), rel.b()))
                .filter(ref -> !ref.equals(options.entityReference()))
                .collect(Collectors.toSet());

        addToPreview(
                preview,
                refs,
                Severity.WARNING,
                "Entity Relationships will be removed");
    }


    private void previewFlowDiagramRemovals(ImmutableTaxonomyChangePreview.Builder preview,
                                            IdSelectionOptions selectionOptions) {

        Set<EntityReference> refs = flowDiagramEntityService
                .findForEntitySelector(selectionOptions)
                .stream()
                .map(FlowDiagramEntity::entityReference)
                .collect(Collectors.toSet());

        addToPreview(
                preview,
                refs,
                Severity.WARNING,
                "Relationships to flow diagrams will be removed");

    }


    private void previewInvolvementRemovals(ImmutableTaxonomyChangePreview.Builder preview,
                                            IdSelectionOptions selectionOptions) {
        addToPreview(
                preview,
                map(involvementService.findByGenericEntitySelector(selectionOptions), Involvement::entityReference),
                Severity.ERROR,
                "Involvements (links to people) associated to this item (or it's children) will be removed");
    }


    private void previewBookmarkRemovals(ImmutableTaxonomyChangePreview.Builder preview,
                                         IdSelectionOptions selectionOptions) {
        Collection<Bookmark> bookmarks = bookmarkService.findByBookmarkIdSelector(selectionOptions);
        addToPreview(
                preview,
                map(bookmarks, Bookmark::entityReference),
                Severity.ERROR,
                "Bookmarks associated to this item (or it's children) will be removed");
    }


    private void previewAppMappingRemovals(ImmutableTaxonomyChangePreview.Builder preview,
                                           IdSelectionOptions selectionOptions) {
        List<MeasurableRating> ratings = measurableRatingService.findByMeasurableIdSelector(selectionOptions);
        addToPreview(
                preview,
                map(ratings, MeasurableRating::entityReference),
                Severity.ERROR,
                "Application ratings associated to this item (or it's children) will be removed");
    }


    private void previewChildNodeRemovals(ImmutableTaxonomyChangePreview.Builder preview,
                                          IdSelectionOptions selectionOptions) {
        Set<EntityReference> childRefs = map(measurableService.findByMeasurableIdSelector(selectionOptions), Measurable::entityReference);
        addToPreview(
                preview,
                minus(childRefs, asSet(selectionOptions.entityReference())),
                Severity.ERROR,
                "This node has child nodes which will also be removed");
    }


    public TaxonomyChangeCommand apply(TaxonomyChangeCommand cmd, String userId) {
        doBasicValidation(cmd);
        validatePrimaryMeasurable(measurableService, cmd);


        IdSelectionOptions selectionOptions = mkOpts(cmd.primaryReference(), HierarchyQueryScope.CHILDREN);

        removeBookmarks(selectionOptions);
        removeInvolvements(selectionOptions);
        removeAppMappings(selectionOptions);
        removeMeasurables(selectionOptions);
        removeFlowDiagrams(selectionOptions);
        removeEntityRelationshipsDiagrams(selectionOptions);

        // TODO: entitySvgDiagrams, roadmapScenarios

        return ImmutableTaxonomyChangeCommand.copyOf(cmd)
                .withStatus(TaxonomyChangeLifecycleStatus.EXECUTED)
                .withLastUpdatedBy(userId)
                .withLastUpdatedAt(DateTimeUtilities.nowUtc());
    }


    private int removeEntityRelationshipsDiagrams(IdSelectionOptions selectionOptions) {
        return entityRelationshipService.deleteForGenericEntitySelector(selectionOptions);
    }


    private int removeFlowDiagrams(IdSelectionOptions selectionOptions) {
        return flowDiagramEntityService.deleteForEntitySelector(selectionOptions);
    }


    private int removeMeasurables(IdSelectionOptions selectionOptions) {
        return measurableService.deleteByIdSelector(selectionOptions);
    }


    private int removeAppMappings(IdSelectionOptions selectionOptions) {
        return measurableRatingService.deleteByMeasurableIdSelector(selectionOptions);
    }


    private int removeInvolvements(IdSelectionOptions selectionOptions) {
        return involvementService.deleteByGenericEntitySelector(selectionOptions);
    }


    private int removeBookmarks(IdSelectionOptions selectionOptions) {
        return bookmarkService.deleteByBookmarkIdSelector(selectionOptions);
    }

}
