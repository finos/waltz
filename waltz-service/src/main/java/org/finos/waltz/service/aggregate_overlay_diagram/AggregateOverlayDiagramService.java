package org.finos.waltz.service.aggregate_overlay_diagram;

import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.aggregate_overlay_diagram.*;
import org.finos.waltz.data.application.ApplicationDao;
import org.finos.waltz.data.cost.CostKindDao;
import org.finos.waltz.data.measurable.MeasurableDao;
import org.finos.waltz.model.*;
import org.finos.waltz.model.aggregate_overlay_diagram.*;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.*;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.AppCostWidgetParameters;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.AppCountWidgetParameters;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.AssessmentWidgetParameters;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.TargetAppCostWidgetParameters;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.cost.EntityCostKind;
import org.finos.waltz.model.measurable.Measurable;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.data.assessment_rating.AssessmentRatingBasedGenericSelectorFactory.applyFilterToSelector;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;

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


    public Set<CountWidgetDatum> findAppCountWidgetData(Long diagramId,
                                                        IdSelectionOptions appSelectionOptions,
                                                        Optional<AssessmentBasedSelectionFilter> filterParams,
                                                        AppCountWidgetParameters appCountWidgetParameters) {

        AggregateOverlayDiagram diagram = aggregateOverlayDiagramDao.getById(diagramId);
        GenericSelector genericSelector = genericSelectorFactory.applyForKind(diagram.aggregatedEntityKind(), appSelectionOptions);
        Select<Record1<Long>> entityIdSelector = applyFilterToSelector(genericSelector, filterParams);

        return appCountWidgetDao.findWidgetData(diagramId, entityIdSelector, appCountWidgetParameters.targetDate());
    }


    public Set<TargetCostWidgetDatum> findTargetAppCostWidgetData(Long diagramId,
                                                                  IdSelectionOptions appSelectionOptions,
                                                                  Optional<AssessmentBasedSelectionFilter> filterParams,
                                                                  TargetAppCostWidgetParameters targetAppCostWidgetParameters) {

        AggregateOverlayDiagram diagram = aggregateOverlayDiagramDao.getById(diagramId);
        ;
        GenericSelector genericSelector = genericSelectorFactory.applyForKind(diagram.aggregatedEntityKind(), appSelectionOptions);

        Select<Record1<Long>> entityIdSelector = applyFilterToSelector(genericSelector, filterParams);
        return targetAppCostWidgetDao.findWidgetData(diagramId, entityIdSelector, targetAppCostWidgetParameters.targetDate());
    }


    public CostWidgetData getAppCostWidgetData(Long diagramId,
                                               Optional<AssessmentBasedSelectionFilter> filterParams,
                                               IdSelectionOptions appSelectionOptions,
                                               AppCostWidgetParameters appCostWidgetParameters) {

        AggregateOverlayDiagram diagram = aggregateOverlayDiagramDao.getById(diagramId);

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(diagram.aggregatedEntityKind(), appSelectionOptions);
        Select<Record1<Long>> entityIdSelector = applyFilterToSelector(genericSelector, filterParams);

        Set<CostWidgetDatum> widgetData = appCostWidgetDao.findWidgetData(
                diagramId,
                appCostWidgetParameters.costKindIds(),
                appCostWidgetParameters.allocationSchemeId(),
                entityIdSelector,
                Optional.empty());

        Set<Row1<Long>> measurableIds = widgetData
                .stream()
                .flatMap(d -> d.measurableCosts().stream())
                .map(MeasurableCostEntry::measurableId)
                .map(DSL::row)
                .collect(Collectors.toSet());

        Table<?> measurableIdTable = DSL.table(measurableIds);

        SelectJoinStep<Record1<Long>> measurableSelector = DSL
                .select(measurableIdTable.field(0, Long.class))
                .from(measurableIdTable);

        List<Measurable> measurables = measurableDao.findByMeasurableIdSelector(measurableSelector);
        List<Application> applications = applicationDao.findByAppIdSelector(entityIdSelector);
        Set<EntityCostKind> costKinds = costKindDao.findAll();

        return ImmutableCostWidgetData.builder()
                .costs(widgetData)
                .measurables(measurables)
                .applications(applications)
                .costKinds(costKinds)
                .build();
    }


    public Set<AssessmentRatingsWidgetDatum> findAppAssessmentWidgetData(Long diagramId,
                                                                         Optional<AssessmentBasedSelectionFilter> filterParams,
                                                                         IdSelectionOptions appSelectionOptions,
                                                                         AssessmentWidgetParameters assessmentWidgetParameters) {

        AggregateOverlayDiagram diagram = aggregateOverlayDiagramDao.getById(diagramId);

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(diagram.aggregatedEntityKind(), appSelectionOptions);
        Select<Record1<Long>> entityIdSelector = applyFilterToSelector(genericSelector, filterParams);

        return appAssessmentWidgetDao.findWidgetData(
                diagramId,
                diagram.aggregatedEntityKind(),
                assessmentWidgetParameters.assessmentDefinitionId(),
                entityIdSelector,
                assessmentWidgetParameters.targetDate());
    }


    public Set<AggregatedEntitiesWidgetDatum> findAggregatedEntitiesWidgetData(Long diagramId,
                                                                               Optional<AssessmentBasedSelectionFilter> filterParams,
                                                                               IdSelectionOptions idSelectionOptions) {

        AggregateOverlayDiagram diagram = aggregateOverlayDiagramDao.getById(diagramId);
        ;
        GenericSelector genericSelector = genericSelectorFactory.applyForKind(diagram.aggregatedEntityKind(), idSelectionOptions);
        Select<Record1<Long>> entityIdSelector = applyFilterToSelector(genericSelector, filterParams);

        return aggregatedEntitiesWidgetDao.findWidgetData(
                diagramId,
                diagram.aggregatedEntityKind(),
                entityIdSelector,
                Optional.empty());
    }


    public Set<BackingEntityWidgetDatum> findBackingEntityWidgetData(Long diagramId) {
        return backingEntityWidgetDao.findWidgetData(diagramId);
    }


    public Set<AggregateOverlayDiagramPreset> findPresetsForDiagram(Long diagramId) {
        return aggregateOverlayDiagramPresetDao.findPresetsForDiagram(diagramId);
    }

    public int createPreset(OverlayDiagramPresetCreateCommand createCommand, String username) {
        return aggregateOverlayDiagramPresetDao.create(createCommand, username);
    }


}
