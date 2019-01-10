package com.khartec.waltz.service.taxonomy_management.processors;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.bookmark.Bookmark;
import com.khartec.waltz.model.measurable.Measurable;
import com.khartec.waltz.model.measurable_rating.MeasurableRating;
import com.khartec.waltz.model.taxonomy_management.*;
import com.khartec.waltz.service.bookmark.BookmarkService;
import com.khartec.waltz.service.involvement.InvolvementService;
import com.khartec.waltz.service.measurable.MeasurableService;
import com.khartec.waltz.service.measurable_rating.MeasurableRatingService;
import com.khartec.waltz.service.taxonomy_management.TaxonomyCommandProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.common.SetUtilities.map;
import static com.khartec.waltz.common.SetUtilities.minus;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static com.khartec.waltz.service.taxonomy_management.TaxonomyManagementUtilities.*;

@Service
public class RemoveMeasurableCommandProcessor implements TaxonomyCommandProcessor {

    private final MeasurableService measurableService;
    private final MeasurableRatingService measurableRatingService;
    private final BookmarkService bookmarkService;
    private final InvolvementService involvementService;


    @Autowired
    public RemoveMeasurableCommandProcessor(MeasurableService measurableService,
                                            MeasurableRatingService measurableRatingService,
                                            InvolvementService involvementService,
                                            BookmarkService bookmarkService) {
        checkNotNull(measurableService, "measurableService cannot be null");
        checkNotNull(measurableRatingService, "measurableRatingService cannot be null");
        checkNotNull(involvementService, "involvementService cannot be null");
        checkNotNull(bookmarkService, "bookmarkService cannot be null");

        this.measurableService = measurableService;
        this.measurableRatingService = measurableRatingService;
        this.involvementService = involvementService;
        this.bookmarkService = bookmarkService;
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

        // TODO: entityRelationships, flowDiagrams, entitySvgDiagrams, roadmapScenarios

        return preview.build();
    }


    private void previewInvolvementRemovals(ImmutableTaxonomyChangePreview.Builder preview,
                                            IdSelectionOptions selectionOptions) {
        addToPreview(
                preview,
                map(involvementService.findByGenericEntitySelector(selectionOptions), r -> r.entityReference()),
                Severity.ERROR,
                "Involvements (links to people) associated to this item (or it's children) will be removed");
    }


    private void previewBookmarkRemovals(ImmutableTaxonomyChangePreview.Builder preview,
                                         IdSelectionOptions selectionOptions) {
        Collection<Bookmark> bookmarks = bookmarkService.findByBookmarkIdSelector(selectionOptions);
        addToPreview(
                preview,
                map(bookmarks, r -> r.entityReference()),
                Severity.ERROR,
                "Bookmarks associated to this item (or it's children) will be removed");
    }


    private void previewAppMappingRemovals(ImmutableTaxonomyChangePreview.Builder preview,
                                           IdSelectionOptions selectionOptions) {
        List<MeasurableRating> ratings = measurableRatingService.findByMeasurableIdSelector(selectionOptions);
        addToPreview(
                preview,
                map(ratings, r -> r.entityReference()),
                Severity.ERROR,
                "Application ratings associated to this item (or it's children) will be removed");
    }


    private void previewChildNodeRemovals(ImmutableTaxonomyChangePreview.Builder preview,
                                          IdSelectionOptions selectionOptions) {
        Set<EntityReference> childRefs = map(measurableService.findByMeasurableIdSelector(selectionOptions), m -> m.entityReference());
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

        // TODO: entityRelationships, flowDiagrams, entitySvgDiagrams, roadmapScenarios

        return ImmutableTaxonomyChangeCommand.copyOf(cmd)
                .withStatus(TaxonomyChangeLifecycleStatus.EXECUTED)
                .withLastUpdatedBy(userId)
                .withLastUpdatedAt(DateTimeUtilities.nowUtc());
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
