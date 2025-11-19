package org.finos.waltz.service.taxonomy_management.processors;

import org.finos.waltz.model.*;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.bookmark.Bookmark;
import org.finos.waltz.model.entity_named_note.EntityNamedNote;
import org.finos.waltz.model.entity_relationship.EntityRelationship;
import org.finos.waltz.model.flow_diagram.FlowDiagramEntity;
import org.finos.waltz.model.involvement.Involvement;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.finos.waltz.model.taxonomy_management.ImmutableTaxonomyChangePreview;
import org.finos.waltz.service.assessment_rating.AssessmentRatingService;
import org.finos.waltz.service.bookmark.BookmarkService;
import org.finos.waltz.service.entity_named_note.EntityNamedNoteService;
import org.finos.waltz.service.entity_relationship.EntityRelationshipService;
import org.finos.waltz.service.flow_diagram.FlowDiagramEntityService;
import org.finos.waltz.service.involvement.InvolvementService;
import org.finos.waltz.service.measurable.MeasurableService;
import org.finos.waltz.service.measurable_rating.MeasurableRatingService;
import org.finos.waltz.service.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.SetUtilities.*;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.service.taxonomy_management.TaxonomyManagementUtilities.addToPreview;

@Service
public class TaxonomyManagementHelper {

    private static final Logger LOG = LoggerFactory.getLogger(TaxonomyManagementHelper.class);

    private final BookmarkService bookmarkService;
    private final EntityRelationshipService entityRelationshipService;
    private final FlowDiagramEntityService flowDiagramEntityService;
    private final InvolvementService involvementService;
    private final MeasurableRatingService measurableRatingService;
    private final MeasurableService measurableService;
    private final EntityNamedNoteService entityNamedNoteService;
    private final AssessmentRatingService assessmentRatingService;

    @Autowired
    public TaxonomyManagementHelper(BookmarkService bookmarkService,
                                    EntityRelationshipService entityRelationshipService,
                                    FlowDiagramEntityService flowDiagramEntityService,
                                    InvolvementService involvementService,
                                    MeasurableRatingService measurableRatingService,
                                    MeasurableService measurableService,
                                    EntityNamedNoteService entityNamedNoteService,
                                    AssessmentRatingService assessmentRatingService) {

        checkNotNull(bookmarkService, "bookmarkService cannot be null");
        checkNotNull(entityRelationshipService, "entityRelationshipService cannot be null");
        checkNotNull(flowDiagramEntityService, "flowDiagramEntityService cannot be null");
        checkNotNull(involvementService, "involvementService cannot be null");
        checkNotNull(measurableRatingService, "measurableRatingService cannot be null");
        checkNotNull(measurableService, "measurableService cannot be null");
        checkNotNull(entityNamedNoteService, "entityNamedNoteService cannot be null");
        checkNotNull(assessmentRatingService, "assessmentRatingService cannot be null");

        this.assessmentRatingService = assessmentRatingService;
        this.bookmarkService = bookmarkService;
        this.entityNamedNoteService = entityNamedNoteService;
        this.entityRelationshipService = entityRelationshipService;
        this.flowDiagramEntityService = flowDiagramEntityService;
        this.involvementService = involvementService;
        this.measurableRatingService = measurableRatingService;
        this.measurableService = measurableService;
    }


    // PREVIEWS

    public void previewEntityRelationships(ImmutableTaxonomyChangePreview.Builder preview,
                                           IdSelectionOptions options) {

        Collection<EntityRelationship> rels = entityRelationshipService.findForGenericEntitySelector(options);

        addToPreview(
                preview,
                rels.size(),
                Severity.WARNING,
                "Entity Relationships will be removed");
    }


    public void previewFlowDiagramRemovals(ImmutableTaxonomyChangePreview.Builder preview,
                                           IdSelectionOptions selectionOptions) {

        List<FlowDiagramEntity> diagrams = flowDiagramEntityService.findForEntitySelector(selectionOptions);

        addToPreview(
                preview,
                diagrams.size(),
                Severity.WARNING,
                "Relationships to flow diagrams will be removed");

    }

    public void previewInvolvementRemovals(ImmutableTaxonomyChangePreview.Builder preview,
                                           IdSelectionOptions selectionOptions) {
        Collection<Involvement> involvements = involvementService.findByGenericEntitySelector(selectionOptions);
        addToPreview(
                preview,
                involvements.size(),
                Severity.ERROR,
                "Involvements (links to people) associated to this item (or it's children) will be removed");
    }

    public void previewEntityNamedNoteRemovals(ImmutableTaxonomyChangePreview.Builder preview,
                                               IdSelectionOptions selectionOptions) {

        List<EntityNamedNote> notes = entityNamedNoteService.findByEntityReference(selectionOptions.entityReference());
        addToPreview(
                preview,
                notes.size(),
                Severity.ERROR,
                "Entity named notes associated to this item will be removed");
    }


    public void previewAssessmentRemovals(ImmutableTaxonomyChangePreview.Builder preview,
                                          IdSelectionOptions selectionOptions) {

        List<AssessmentRating> assessmentRatings = assessmentRatingService.findByTargetKindForRelatedSelector(EntityKind.MEASURABLE, selectionOptions);
        addToPreview(
                preview,
                assessmentRatings.size(),
                Severity.ERROR,
                "Assessments associated to this item will be removed");
    }


    public void previewBookmarkRemovals(ImmutableTaxonomyChangePreview.Builder preview,
                                        IdSelectionOptions selectionOptions) {
        Set<Bookmark> bookmarks = bookmarkService.findByBookmarkIdSelector(selectionOptions);
        addToPreview(
                preview,
                bookmarks.size(),
                Severity.ERROR,
                "Bookmarks associated to this item will be removed");
    }


    public void previewAppMappingRemovals(ImmutableTaxonomyChangePreview.Builder preview,
                                          IdSelectionOptions selectionOptions) {
        List<MeasurableRating> ratings = measurableRatingService.findByMeasurableIdSelector(selectionOptions);
        addToPreview(
                preview,
                ratings.size(),
                Severity.ERROR,
                "Application ratings associated to this item (or it's children) will be removed");
    }


    public void previewChildNodeRemovals(ImmutableTaxonomyChangePreview.Builder preview,
                                         IdSelectionOptions selectionOptions) {
        Set<EntityReference> childRefs = map(measurableService.findByMeasurableIdSelector(selectionOptions), Measurable::entityReference);
        addToPreview(
                preview,
                minus(childRefs, asSet(selectionOptions.entityReference())).size(),
                Severity.ERROR,
                "This node has child nodes which will also be removed");
    }

    public void previewChildNodeMigrations(ImmutableTaxonomyChangePreview.Builder preview,
                                           IdSelectionOptions selectionOptions) {

        ImmutableIdSelectionOptions hierarchySelector = ImmutableIdSelectionOptions.copyOf(selectionOptions).withScope(HierarchyQueryScope.CHILDREN);

        Set<EntityReference> childRefs = map(measurableService.findByMeasurableIdSelector(hierarchySelector), Measurable::entityReference);

        addToPreview(
                preview,
                minus(childRefs, asSet(selectionOptions.entityReference())).size(),
                Severity.WARNING,
                "This node has child nodes which will be migrated to the new target");
    }

    public void previewRatingMigrations(ImmutableTaxonomyChangePreview.Builder preview,
                                        Long measurableId,
                                        Long targetId) {

        int sharedRatingsCount = measurableRatingService.getSharedRatingsCount(measurableId, targetId);

        addToPreview(
                preview,
                sharedRatingsCount,
                Severity.WARNING,
                "This node has measurable ratings, kindly note that the higher rating will take precedence.");
    }

    public void previewDecommMigrations(ImmutableTaxonomyChangePreview.Builder preview,
                                        Long measurableId,
                                        Long targetId) {

        int sharedDecommsCount = measurableRatingService.getSharedDecommsCount(measurableId, targetId);

        addToPreview(
                preview,
                sharedDecommsCount,
                Severity.WARNING,
                "This node has measurable rating planned decommission dates that cannot be migrated due to an existing decom on the target rating");
    }


    // ACTIONS

    public void migrateMeasurable(IdSelectionOptions selectionOptions, Long targetId, String userId, long measurableCategoryId) {

        EntityReference sourceMeasurable = selectionOptions.entityReference();
        EntityReference targetMeasurable = mkRef(EntityKind.MEASURABLE, targetId);

        measurableService.moveChildren(sourceMeasurable.id(), targetId, userId);
        measurableRatingService.migrateRatings(sourceMeasurable.id(), targetId, userId, measurableCategoryId);
        entityRelationshipService.migrateEntityRelationships(sourceMeasurable, targetMeasurable, userId);
        measurableService.deleteByIdSelector(selectionOptions);
    }

    public int removeEntityRelationshipsDiagrams(IdSelectionOptions selectionOptions) {
        return entityRelationshipService.deleteForGenericEntitySelector(selectionOptions);
    }


    public int removeFlowDiagrams(IdSelectionOptions selectionOptions) {
        return flowDiagramEntityService.deleteForEntitySelector(selectionOptions);
    }


    public int removeMeasurables(IdSelectionOptions selectionOptions) {
        return measurableService.deleteByIdSelector(selectionOptions);
    }


    public int removeAppMappings(IdSelectionOptions selectionOptions) {
        return measurableRatingService.deleteByMeasurableIdSelector(selectionOptions);
    }


    public int removeInvolvements(IdSelectionOptions selectionOptions) {
        return involvementService.deleteByGenericEntitySelector(selectionOptions);
    }


    public int removeBookmarks(IdSelectionOptions selectionOptions) {
        return bookmarkService.deleteByBookmarkIdSelector(selectionOptions);
    }


    public int removeAssessments(EntityKind targetKind, IdSelectionOptions selectionOptions) {
        return assessmentRatingService.deleteByAssessmentRatingRelatedSelector(targetKind, selectionOptions);
    }

    public int removeNamedNotes(IdSelectionOptions selectionOptions) {
        return entityNamedNoteService.deleteByNamedNoteParentSelector(selectionOptions);
    }

}
