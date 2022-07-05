package org.finos.waltz.service.aggregate_overlay_diagram;

import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramDao;
import org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramPresetDao;
import org.finos.waltz.data.aggregate_overlay_diagram.AggregatedEntitiesWidgetDao;
import org.finos.waltz.data.aggregate_overlay_diagram.AppCostWidgetDao;
import org.finos.waltz.data.aggregate_overlay_diagram.AppCountWidgetDao;
import org.finos.waltz.data.aggregate_overlay_diagram.AssessmentRatingWidgetDao;
import org.finos.waltz.data.aggregate_overlay_diagram.BackingEntityWidgetDao;
import org.finos.waltz.data.aggregate_overlay_diagram.TargetAppCostWidgetDao;
import org.finos.waltz.data.application.ApplicationDao;
import org.finos.waltz.data.cost.CostKindDao;
import org.finos.waltz.data.measurable.MeasurableDao;
import org.finos.waltz.model.AssessmentBasedSelectionFilter;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.aggregate_overlay_diagram.AggregateOverlayDiagram;
import org.finos.waltz.model.aggregate_overlay_diagram.AggregateOverlayDiagramInfo;
import org.finos.waltz.model.aggregate_overlay_diagram.AggregateOverlayDiagramPreset;
import org.finos.waltz.model.aggregate_overlay_diagram.BackingEntity;
import org.finos.waltz.model.aggregate_overlay_diagram.ImmutableAggregateOverlayDiagramInfo;
import org.finos.waltz.model.aggregate_overlay_diagram.OverlayDiagramPresetCreateCommand;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.*;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.AppCostWidgetParameters;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.AppCountWidgetParameters;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.AssessmentWidgetParameters;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.TargetAppCostWidgetParameters;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.cost.EntityCostKind;
import org.finos.waltz.model.measurable.Measurable;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.data.assessment_rating.AssessmentRatingBasedGenericSelectorFactory.applyFiltersToSelector;
import static org.finos.waltz.schema.Tables.MEASURABLE;

@Service
public class AggregateOverlayDiagramService {


    private final AggregateOverlayDiagramDao aggregateOverlayDiagramDao;
    private final AppCountWidgetDao appCountWidgetDao;
    private final TargetAppCostWidgetDao targetAppCostWidgetDao;
    private final AppCostWidgetDao appCostWidgetDao;
    private final AssessmentRatingWidgetDao appAssessmentWidgetDao;
    private final BackingEntityWidgetDao backingEntityWidgetDao;
    private final AggregatedEntitiesWidgetDao aggregatedEntitiesWidgetDao;
    private final AggregateOverlayDiagramPresetDao aggregateOverlayDiagramPresetDao;
    private final MeasurableDao measurableDao;
    private final ApplicationDao applicationDao;
    private final CostKindDao costKindDao;

    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();

    @Autowired
    public AggregateOverlayDiagramService(AggregateOverlayDiagramDao aggregateOverlayDiagramDao,
                                          AppCountWidgetDao appCountWidgetDao,
                                          TargetAppCostWidgetDao targetAppCostWidgetDao,
                                          AssessmentRatingWidgetDao appAssessmentWidgetDao,
                                          BackingEntityWidgetDao backingEntityWidgetDao,
                                          AppCostWidgetDao appCostWidgetDao,
                                          AggregatedEntitiesWidgetDao aggregatedEntitiesWidgetDao,
                                          AggregateOverlayDiagramPresetDao aggregateOverlayDiagramPresetDao,
                                          MeasurableDao measurableDao,
                                          ApplicationDao applicationDao,
                                          CostKindDao costKindDao) {

        this.aggregateOverlayDiagramDao = aggregateOverlayDiagramDao;
        this.appCountWidgetDao = appCountWidgetDao;
        this.targetAppCostWidgetDao = targetAppCostWidgetDao;
        this.appCostWidgetDao = appCostWidgetDao;
        this.appAssessmentWidgetDao = appAssessmentWidgetDao;
        this.backingEntityWidgetDao = backingEntityWidgetDao;
        this.aggregatedEntitiesWidgetDao = aggregatedEntitiesWidgetDao;
        this.aggregateOverlayDiagramPresetDao = aggregateOverlayDiagramPresetDao;
        this.measurableDao = measurableDao;
        this.applicationDao = applicationDao;
        this.costKindDao = costKindDao;
    }


    public AggregateOverlayDiagramInfo getById(Long diagramId) {
        AggregateOverlayDiagram diagram = aggregateOverlayDiagramDao.getById(diagramId);
        Set<BackingEntity> backingEntities = aggregateOverlayDiagramDao.findBackingEntities(diagramId);

        return ImmutableAggregateOverlayDiagramInfo.builder()
                .diagram(diagram)
                .backingEntities(backingEntities)
                .build();
    }


    public Set<AggregateOverlayDiagram> findAll() {
        return aggregateOverlayDiagramDao.findAll();
    }


    public CountWidgetData getAppCountWidgetData(Long diagramId,
                                                 IdSelectionOptions appSelectionOptions,
                                                 Set<AssessmentBasedSelectionFilter> filterParams,
                                                 AppCountWidgetParameters appCountWidgetParameters) {

        AggregateOverlayDiagram diagram = aggregateOverlayDiagramDao.getById(diagramId);
        GenericSelector genericSelector = genericSelectorFactory.applyForKind(diagram.aggregatedEntityKind(), appSelectionOptions);
        Select<Record1<Long>> entityIdSelector = applyFiltersToSelector(genericSelector, filterParams);

        Set<CountWidgetDatum> countData = appCountWidgetDao
                .findWidgetData(
                        diagramId,
                        entityIdSelector,
                        appCountWidgetParameters.targetDate());

        return ImmutableCountWidgetData
                .builder()
                .cellData(countData)
                .build();
    }


    public TargetCostWidgetData findTargetAppCostWidgetData(Long diagramId,
                                                            IdSelectionOptions appSelectionOptions,
                                                            Set<AssessmentBasedSelectionFilter> filterParams,
                                                            TargetAppCostWidgetParameters targetAppCostWidgetParameters) {

        AggregateOverlayDiagram diagram = aggregateOverlayDiagramDao.getById(diagramId);

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(diagram.aggregatedEntityKind(), appSelectionOptions);

        Select<Record1<Long>> entityIdSelector = applyFiltersToSelector(genericSelector, filterParams);
        Set<TargetCostWidgetDatum> targetCostData = targetAppCostWidgetDao.findWidgetData(diagramId, entityIdSelector, targetAppCostWidgetParameters.targetDate());

        return ImmutableTargetCostWidgetData
                .builder()
                .cellData(targetCostData)
                .build();
    }


    public CostWidgetData getAppCostWidgetData(Long diagramId,
                                               Set<AssessmentBasedSelectionFilter> filterParams,
                                               IdSelectionOptions appSelectionOptions,
                                               AppCostWidgetParameters appCostWidgetParameters) {

        AggregateOverlayDiagram diagram = aggregateOverlayDiagramDao.getById(diagramId);

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(diagram.aggregatedEntityKind(), appSelectionOptions);
        Select<Record1<Long>> entityIdSelector = applyFiltersToSelector(genericSelector, filterParams);

        Set<CostWidgetDatum> costData = appCostWidgetDao.findWidgetData(
                diagramId,
                appCostWidgetParameters.costKindIds(),
                appCostWidgetParameters.allocationSchemeId(),
                entityIdSelector,
                Optional.empty());

        Set<Long> measurableIds = costData
                .stream()
                .flatMap(d -> d
                        .measurableCosts()
                        .stream())
                .map(MeasurableCostEntry::measurableId)
                .collect(Collectors.toSet());

        Select<Record1<Long>> measurableSelector = DSL
                .select(MEASURABLE.ID)
                .from(MEASURABLE)
                .where(MEASURABLE.ID.in(measurableIds));

        List<Measurable> measurables = measurableDao.findByMeasurableIdSelector(measurableSelector);
        List<Application> applications = applicationDao.findByAppIdSelector(entityIdSelector);
        Set<EntityCostKind> costKinds = costKindDao.findAll();

        return ImmutableCostWidgetData.builder()
                .cellData(costData)
                .measurables(measurables)
                .applications(applications)
                .costKinds(costKinds)
                .build();
    }


    public AssessmentRatingsWidgetData getAppAssessmentWidgetData(Long diagramId,
                                                                  Set<AssessmentBasedSelectionFilter> filterParams,
                                                                  IdSelectionOptions appSelectionOptions,
                                                                  AssessmentWidgetParameters assessmentWidgetParameters) {

        AggregateOverlayDiagram diagram = aggregateOverlayDiagramDao.getById(diagramId);

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(diagram.aggregatedEntityKind(), appSelectionOptions);
        Select<Record1<Long>> entityIdSelector = applyFiltersToSelector(genericSelector, filterParams);

        return ImmutableAssessmentRatingsWidgetData.builder()
                .cellData(appAssessmentWidgetDao.findWidgetData(
                        diagramId,
                        diagram.aggregatedEntityKind(),
                        assessmentWidgetParameters.assessmentDefinitionId(),
                        entityIdSelector,
                        assessmentWidgetParameters.targetDate()))
               .build();
    }


    public AggregatedEntitiesWidgetData getAggregatedEntitiesWidgetData(Long diagramId,
                                                                        Set<AssessmentBasedSelectionFilter> filterParams,
                                                                        IdSelectionOptions idSelectionOptions) {

        AggregateOverlayDiagram diagram = aggregateOverlayDiagramDao.getById(diagramId);

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(diagram.aggregatedEntityKind(), idSelectionOptions);
        Select<Record1<Long>> entityIdSelector = applyFiltersToSelector(genericSelector, filterParams);

        Set<AggregatedEntitiesWidgetDatum> data = aggregatedEntitiesWidgetDao.findWidgetData(
                diagramId,
                diagram.aggregatedEntityKind(),
                entityIdSelector,
                Optional.empty());

        return ImmutableAggregatedEntitiesWidgetData.builder()
                .cellData(data)
                .build();
    }


    public BackingEntityWidgetData getBackingEntityWidgetData(Long diagramId) {
        return ImmutableBackingEntityWidgetData
                .builder()
                .cellData(backingEntityWidgetDao.findWidgetData(diagramId))
                .build();
    }


    public Set<AggregateOverlayDiagramPreset> findPresetsForDiagram(Long diagramId) {
        return aggregateOverlayDiagramPresetDao.findPresetsForDiagram(diagramId);
    }

    public int createPreset(OverlayDiagramPresetCreateCommand createCommand, String username) {
        return aggregateOverlayDiagramPresetDao.create(createCommand, username);
    }


}
