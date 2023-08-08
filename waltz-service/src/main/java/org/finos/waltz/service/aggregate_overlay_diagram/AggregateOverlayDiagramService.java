package org.finos.waltz.service.aggregate_overlay_diagram;

import org.finos.waltz.common.Checks;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.aggregate_overlay_diagram.*;
import org.finos.waltz.data.application.ApplicationDao;
import org.finos.waltz.data.complexity.ComplexityKindDao;
import org.finos.waltz.data.cost.CostKindDao;
import org.finos.waltz.data.measurable.MeasurableDao;
import org.finos.waltz.model.AssessmentBasedSelectionFilter;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.ReleaseLifecycleStatusChangeCommand;
import org.finos.waltz.model.aggregate_overlay_diagram.*;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.*;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.*;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.complexity.ComplexityKind;
import org.finos.waltz.model.cost.CostKindWithYears;
import org.finos.waltz.model.entity_overlay_diagram.OverlayDiagramKind;
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
    private final ComplexityKindDao complexityKindDao;
    private final ComplexityWidgetDao complexityWidgetDao;
    private final AttestationWidgetDao attestationWidgetDao;

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
                                          CostKindDao costKindDao,
                                          ComplexityKindDao complexityKindDao,
                                          ComplexityWidgetDao complexityWidgetDao,
                                          AttestationWidgetDao attestationWidgetDao) {

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
        this.complexityKindDao = complexityKindDao;
        this.complexityWidgetDao = complexityWidgetDao;
        this.attestationWidgetDao = attestationWidgetDao;
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


    public Set<AggregateOverlayDiagram> findByKind(OverlayDiagramKind kind) {
        Checks.checkNotNull(kind, "OverlayDiagramKind cannot be null");
        return aggregateOverlayDiagramDao.findByKind(kind);
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


    public TargetCostWidgetData getTargetAppCostWidgetData(Long diagramId,
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

    public AttestationWidgetData getAttestationWidgetData(long diagramId,
                                                          Set<AssessmentBasedSelectionFilter> filterParams,
                                                          IdSelectionOptions appSelectionOptions,
                                                          AttestationWidgetParameters widgetParams) {


        AggregateOverlayDiagram diagram = aggregateOverlayDiagramDao.getById(diagramId);

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(diagram.aggregatedEntityKind(), appSelectionOptions);
        Select<Record1<Long>> entityIdSelector = applyFiltersToSelector(genericSelector, filterParams);

        Set<AttestationWidgetDatum> attestations = attestationWidgetDao.findWidgetData(
                diagramId,
                widgetParams.attestedEntityKind(),
                Optional.ofNullable(widgetParams.attestedEntityId()),
                entityIdSelector);

        List<Application> applications = applicationDao.findByAppIdSelector(entityIdSelector);

        return ImmutableAttestationWidgetData
                .builder()
                .cellData(attestations)
                .applications(applications)
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
                entityIdSelector);

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
        Set<CostKindWithYears> costKindsWithYears = costKindDao.findAll();

        return ImmutableCostWidgetData
                .builder()
                .cellData(costData)
                .measurables(measurables)
                .applications(applications)
                .costKinds(SetUtilities.map(costKindsWithYears, CostKindWithYears::costKind))
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


    public ComplexityWidgetData getAppComplexityWidgetData(long diagramId,
                                                           Set<AssessmentBasedSelectionFilter> assessmentBasedSelectionFilters,
                                                           IdSelectionOptions idSelectionOptions,
                                                           AppComplexityWidgetParameters complexityWidgetParameters) {


        AggregateOverlayDiagram diagram = aggregateOverlayDiagramDao.getById(diagramId);

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(diagram.aggregatedEntityKind(), idSelectionOptions);
        Select<Record1<Long>> entityIdSelector = applyFiltersToSelector(genericSelector, assessmentBasedSelectionFilters);

        Set<ComplexityWidgetDatum> complexityData = complexityWidgetDao
                .findWidgetData(
                        diagramId,
                        diagram.aggregatedEntityKind(),
                        complexityWidgetParameters.complexityKindIds(),
                        entityIdSelector);

        List<Application> applications = applicationDao.findByAppIdSelector(entityIdSelector);
        Set<ComplexityKind> complexityKinds = complexityKindDao.findAll();

        return ImmutableComplexityWidgetData
                .builder()
                .cellData(complexityData)
                .applications(applications)
                .complexityKinds(complexityKinds)
                .build();
    }

    public Long create(OverlayDiagramCreateCommand createCmd, String username) {
        Long diagramId = aggregateOverlayDiagramDao.save(createCmd, username);
        aggregateOverlayDiagramDao.updateBackingEntities(diagramId, createCmd.backingEntities());
        return diagramId;
    }

    public Boolean updateStatus(long diagramId, ReleaseLifecycleStatusChangeCommand changeStatusCmd, String username) {
        return aggregateOverlayDiagramDao
                .updateStatus(diagramId, changeStatusCmd);
    }
}
