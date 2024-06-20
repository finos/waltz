package org.finos.waltz.service.measurable_rating;

import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.measurable.MeasurableDao;
import org.finos.waltz.data.measurable_rating.MeasurableRatingIdSelectorFactory;
import org.finos.waltz.data.rating_scheme.RatingSchemeDAO;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.allocation.Allocation;
import org.finos.waltz.model.allocation_scheme.AllocationScheme;
import org.finos.waltz.model.application.AssessmentsView;
import org.finos.waltz.model.application.ImmutableAssessmentsView;
import org.finos.waltz.model.application.ImmutableMeasurableRatingsView;
import org.finos.waltz.model.application.MeasurableRatingsView;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_definition.AssessmentVisibility;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.measurable.MeasurableHierarchy;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.finos.waltz.model.measurable_rating.AllocationsView;
import org.finos.waltz.model.measurable_rating.DecommissionsView;
import org.finos.waltz.model.measurable_rating.ImmutableAllocationsView;
import org.finos.waltz.model.measurable_rating.ImmutableDecommissionsView;
import org.finos.waltz.model.measurable_rating.ImmutableMeasurableRatingCategoryView;
import org.finos.waltz.model.measurable_rating.ImmutableMeasurableRatingView;
import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.finos.waltz.model.measurable_rating.MeasurableRatingCategoryView;
import org.finos.waltz.model.measurable_rating.MeasurableRatingView;
import org.finos.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommission;
import org.finos.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommissionInfo;
import org.finos.waltz.model.measurable_rating_replacement.MeasurableRatingReplacement;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.service.allocation.AllocationService;
import org.finos.waltz.service.allocation_schemes.AllocationSchemeService;
import org.finos.waltz.service.application.ApplicationService;
import org.finos.waltz.service.assessment_definition.AssessmentDefinitionService;
import org.finos.waltz.service.assessment_rating.AssessmentRatingService;
import org.finos.waltz.service.measurable.MeasurableService;
import org.finos.waltz.service.measurable_category.MeasurableCategoryService;
import org.finos.waltz.service.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommissionService;
import org.finos.waltz.service.measurable_rating_replacement.MeasurableRatingReplacementService;
import org.finos.waltz.service.rating_scheme.RatingSchemeService;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.finos.waltz.common.FunctionUtilities.time;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.SetUtilities.filter;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.model.utils.IdUtilities.toIds;
import static org.finos.waltz.schema.Tables.MEASURABLE;

@Service
public class MeasurableRatingViewService {

    private static final MeasurableRatingIdSelectorFactory MEASURABLE_RATING_ID_SELECTOR_FACTORY = new MeasurableRatingIdSelectorFactory();
    private static final GenericSelectorFactory GENERIC_SELECTOR_FACTORY = new GenericSelectorFactory();

    private final MeasurableRatingService measurableRatingService;
    private final MeasurableService measurableService;
    private final MeasurableDao measurableDao;
    private final MeasurableRatingPlannedDecommissionService measurableRatingPlannedDecommissionService;
    private final MeasurableRatingReplacementService measurableRatingReplacementService;
    private final RatingSchemeService ratingSchemeService;
    private final RatingSchemeDAO ratingSchemeDAO;


    private final MeasurableCategoryService measurableCategoryService;
    private final AssessmentRatingService assessmentRatingService;
    private final AssessmentDefinitionService assessmentDefinitionService;
    private final AllocationService allocationService;
    private final AllocationSchemeService allocationSchemeService;
    private final ApplicationService applicationService;



    public MeasurableRatingViewService(MeasurableRatingService measurableRatingService,
                                       MeasurableService measurableService,
                                       MeasurableDao measurableDao,
                                       MeasurableRatingPlannedDecommissionService measurableRatingPlannedDecommissionService,
                                       MeasurableRatingReplacementService measurableRatingReplacementService,
                                       RatingSchemeService ratingSchemeService,
                                       RatingSchemeDAO ratingSchemeDAO,
                                       MeasurableCategoryService measurableCategoryService,
                                       AssessmentRatingService assessmentRatingService,
                                       AssessmentDefinitionService assessmentDefinitionService,
                                       AllocationService allocationService,
                                       AllocationSchemeService allocationSchemeService,
                                       ApplicationService applicationService){

        this.measurableRatingService = measurableRatingService;
        this.measurableService = measurableService;
        this.measurableDao = measurableDao;
        this.measurableRatingPlannedDecommissionService = measurableRatingPlannedDecommissionService;
        this.measurableRatingReplacementService = measurableRatingReplacementService;
        this.ratingSchemeService = ratingSchemeService;
        this.ratingSchemeDAO = ratingSchemeDAO;
        this.measurableCategoryService = measurableCategoryService;
        this.assessmentRatingService = assessmentRatingService;
        this.assessmentDefinitionService = assessmentDefinitionService;
        this.allocationService = allocationService;
        this.allocationSchemeService = allocationSchemeService;
        this.applicationService = applicationService;
    }


    public MeasurableRatingView getViewById(long id) {

        MeasurableRating measurableRating = measurableRatingService.getById(id);

        Measurable measurable = measurableService.getById(measurableRating.measurableId());

        if (measurable == null) {

            return ImmutableMeasurableRatingView
                    .builder()
                    .measurableRating(measurableRating)
                    .measurable(null)
                    .rating(null)
                    .decommission(null)
                    .replacements(emptyList())
                    .category(null)
                    .build();
        } else {

            MeasurableCategory category = measurableCategoryService.getById(measurable.categoryId());

            List<RatingSchemeItem> ratingSchemeItems = ratingSchemeService.findRatingSchemeItemsForEntityAndCategory(
                    measurableRating.entityReference(),
                    measurable.categoryId());

            Map<String, RatingSchemeItem> itemsByCode = indexBy(ratingSchemeItems, RatingSchemeItem::rating);
            RatingSchemeItem rating = itemsByCode.get(String.valueOf(measurableRating.rating()));

            MeasurableRatingPlannedDecommission decomm = measurableRatingPlannedDecommissionService.getByMeasurableRatingId(id);

            Set<MeasurableRatingReplacement> replacementApps = decomm == null
                    ? Collections.emptySet()
                    : measurableRatingReplacementService.findByDecommId(decomm.id());

            List<AllocationScheme> schemes = allocationSchemeService.findByCategoryId(measurable.categoryId());

            Set<Allocation> allocations = allocationService.findByMeasurableRatingId(id);

            return ImmutableMeasurableRatingView.builder()
                    .measurableRating(measurableRating)
                    .measurable(measurable)
                    .rating(rating)
                    .decommission(decomm)
                    .replacements(replacementApps)
                    .allocations(allocations)
                    .allocationSchemes(schemes)
                    .category(category)
                    .build();
        }

    }

    public MeasurableRatingCategoryView getViewForCategoryAndSelector(IdSelectionOptions idSelectionOptions,
                                                                      long categoryId) {


        Select<Record1<Long>> ratingIdSelector = MEASURABLE_RATING_ID_SELECTOR_FACTORY.apply(idSelectionOptions);

        MeasurableCategory category = measurableCategoryService.getById(categoryId);

        if(category == null) {
            throw new IllegalArgumentException(format("Cannot find category with id: %s", categoryId));
        }

        List<MeasurableRating> ratings = time("rating", () -> measurableRatingService.findForCategoryAndMeasurableRatingIdSelector(ratingIdSelector, categoryId));
        List<Measurable> measurables = time("measurables", () -> measurableService.findByCategoryId(categoryId));
        List<AllocationScheme> allocSchemes = time("allocSch", () -> allocationSchemeService.findByCategoryId(categoryId));
        Collection<Allocation> allocs = time("allocs", () -> allocationService.findForCategoryAndMeasurableRatingIdSelector(ratingIdSelector, categoryId));

        Set<AssessmentDefinition> defs = filter(
                assessmentDefinitionService.findByEntityKind(EntityKind.MEASURABLE_RATING),
                d -> d.visibility().equals(AssessmentVisibility.PRIMARY)
                        && d.qualifierReference()
                        .map(qualifierRef -> qualifierRef.id() == categoryId)
                        .orElse(false));

        List<AssessmentRating> assessments = time("assessments", () -> assessmentRatingService.findByEntityKind(EntityKind.MEASURABLE_RATING));
        Set<RatingSchemeItem> assessmentRatingSchemeItems = time("arsi", () -> ratingSchemeDAO.findRatingSchemeItemsByIds(map(assessments, AssessmentRating::ratingId)));
        Set<RatingSchemeItem> measurableRatingSchemeItems = time("mrsi", () -> ratingSchemeDAO.findRatingSchemeItemsForSchemeIds(asSet(category.ratingSchemeId())));
        Collection<MeasurableRatingPlannedDecommission> decomms = time("decom", () -> measurableRatingPlannedDecommissionService.findForCategoryAndMeasurableRatingIdSelector(ratingIdSelector, categoryId));
        Collection<MeasurableRatingReplacement> replacements = time("replace", () -> measurableRatingReplacementService.findForCategoryAndMeasurableRatingIdSelector(ratingIdSelector, categoryId));

        GenericSelector replacementSubjectIdSelector = GENERIC_SELECTOR_FACTORY.apply(idSelectionOptions);

        Collection<MeasurableRatingPlannedDecommissionInfo> replacingDecomms = time("replacing", () -> measurableRatingPlannedDecommissionService.findForReplacingSubjectIdSelectorAndCategory(replacementSubjectIdSelector, categoryId));
        Set<AssessmentRating> assessmentRatings = filter(assessments, d -> toIds(defs).contains(d.assessmentDefinitionId()));

        Set<MeasurableHierarchy> hierarchyForCategory = time("hier", () -> measurableService.findHierarchyForCategory(categoryId));

        MeasurableRatingsView primaryRatingsView = time("prim", () -> getPrimaryRatingsViewForMeasurableIdSelector(ratingIdSelector));

        MeasurableRatingsView ratingsView = ImmutableMeasurableRatingsView
                .builder()
                .measurableRatings(ratings)
                .measurables(measurables)
                .measurableCategories(asSet(category))
                .measurableHierarchy(hierarchyForCategory)
                .ratingSchemeItems(measurableRatingSchemeItems)
                .build();

        AssessmentsView assessmentsView = ImmutableAssessmentsView
                .builder()
                .assessmentRatings(assessmentRatings)
                .assessmentDefinitions(defs)
                .ratingSchemeItems(assessmentRatingSchemeItems)
                .build();

        AllocationsView allocationsView = ImmutableAllocationsView
                .builder()
                .allocations(allocs)
                .allocationSchemes(allocSchemes)
                .build();

        DecommissionsView decommissionView = ImmutableDecommissionsView
                .builder()
                .plannedDecommissions(decomms)
                .plannedReplacements(replacements)
                .replacingDecommissions(replacingDecomms)
                .build();

        return ImmutableMeasurableRatingCategoryView.builder()
                .measurableRatings(ratingsView)
                .allocations(allocationsView)
                .primaryAssessments(assessmentsView)
                .decommissions(decommissionView)
                .primaryRatings(primaryRatingsView)
                .build();
    }


    /*
     * Should move to using a measurable rating id selector
     */
    @Deprecated
    private MeasurableRatingsView getPrimaryRatingsView(GenericSelector subjectIdSelector) {

        Set<MeasurableCategory> primaryCategories = measurableCategoryService.findAll()
                .stream()
                .filter(MeasurableCategory::allowPrimaryRatings)
                .collect(Collectors.toSet());

        Set<MeasurableRating> primaryMeasurableRatings = measurableRatingService.findPrimaryRatingsForGenericSelector(subjectIdSelector);
        List<Measurable> primaryMeasurables = measurableDao.findByMeasurableIdSelector(mkMeasurableIdSelector(primaryMeasurableRatings));
        Set<RatingSchemeItem> primaryRatingSchemeItems = ratingSchemeDAO.findRatingSchemeItemsForSchemeIds(map(primaryCategories, MeasurableCategory::ratingSchemeId));

        return ImmutableMeasurableRatingsView.builder()
                .measurableRatings(primaryMeasurableRatings)
                .measurables(primaryMeasurables)
                .measurableCategories(primaryCategories)
                .ratingSchemeItems(primaryRatingSchemeItems)
                .build();
    }

    private MeasurableRatingsView getPrimaryRatingsViewForMeasurableIdSelector(Select<Record1<Long>> ratingIdSelector) {

        Set<MeasurableCategory> primaryCategories = measurableCategoryService.findAll()
                .stream()
                .filter(MeasurableCategory::allowPrimaryRatings)
                .collect(Collectors.toSet());

        Set<MeasurableRating> primaryMeasurableRatings = measurableRatingService.findPrimaryRatingsForMeasurableIdSelector(ratingIdSelector);
        List<Measurable> primaryMeasurables = measurableDao.findByMeasurableIdSelector(mkMeasurableIdSelector(primaryMeasurableRatings));
        Set<RatingSchemeItem> primaryRatingSchemeItems = ratingSchemeDAO.findRatingSchemeItemsForSchemeIds(map(primaryCategories, MeasurableCategory::ratingSchemeId));

        return ImmutableMeasurableRatingsView.builder()
                .measurableRatings(primaryMeasurableRatings)
                .measurables(primaryMeasurables)
                .measurableCategories(primaryCategories)
                .ratingSchemeItems(primaryRatingSchemeItems)
                .build();
    }


    private SelectConditionStep<Record1<Long>> mkMeasurableIdSelector(Set<MeasurableRating> primaryMeasurableRatings) {
        return DSL
                .select(MEASURABLE.ID)
                .from(MEASURABLE)
                .where(MEASURABLE.ID.in(map(
                        primaryMeasurableRatings,
                        MeasurableRating::measurableId)));
    }


    public MeasurableRatingsView getPrimaryRatingsView(IdSelectionOptions idSelectionOptions) {
        GenericSelectorFactory selectorFactory = new GenericSelectorFactory();
        GenericSelector genericSelector = selectorFactory.apply(idSelectionOptions);
        return getPrimaryRatingsView(genericSelector);
    }
}
