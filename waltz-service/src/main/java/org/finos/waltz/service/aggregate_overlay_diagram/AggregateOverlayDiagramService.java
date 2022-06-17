package org.finos.waltz.service.aggregate_overlay_diagram;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.aggregate_overlay_diagram.*;
import org.finos.waltz.model.AssessmentBasedSelectionFilter;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.aggregate_overlay_diagram.*;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.*;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.AppCostWidgetParameters;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.AppCountWidgetParameters;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.AssessmentWidgetParameters;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.TargetAppCostWidgetParameters;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

import static org.finos.waltz.data.assessment_rating.AssessmentRatingBasedGenericSelectorFactory.applyFiltersToSelector;

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

    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();

    @Autowired
    public AggregateOverlayDiagramService(AggregateOverlayDiagramDao aggregateOverlayDiagramDao,
                                          AppCountWidgetDao appCountWidgetDao,
                                          TargetAppCostWidgetDao targetAppCostWidgetDao,
                                          AssessmentRatingWidgetDao appAssessmentWidgetDao,
                                          BackingEntityWidgetDao backingEntityWidgetDao,
                                          AppCostWidgetDao appCostWidgetDao,
                                          AggregatedEntitiesWidgetDao aggregatedEntitiesWidgetDao,
                                          AggregateOverlayDiagramPresetDao aggregateOverlayDiagramPresetDao) {
        this.aggregateOverlayDiagramDao = aggregateOverlayDiagramDao;
        this.appCountWidgetDao = appCountWidgetDao;
        this.targetAppCostWidgetDao = targetAppCostWidgetDao;
        this.appCostWidgetDao = appCostWidgetDao;
        this.appAssessmentWidgetDao = appAssessmentWidgetDao;
        this.backingEntityWidgetDao = backingEntityWidgetDao;
        this.aggregatedEntitiesWidgetDao = aggregatedEntitiesWidgetDao;
        this.aggregateOverlayDiagramPresetDao = aggregateOverlayDiagramPresetDao;
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
                                                        Set<AssessmentBasedSelectionFilter> filterParams,
                                                        AppCountWidgetParameters appCountWidgetParameters) {

        AggregateOverlayDiagram diagram = aggregateOverlayDiagramDao.getById(diagramId);
        GenericSelector genericSelector = genericSelectorFactory.applyForKind(diagram.aggregatedEntityKind(), appSelectionOptions);
        Select<Record1<Long>> entityIdSelector = applyFiltersToSelector(genericSelector, filterParams);

        return appCountWidgetDao.findWidgetData(diagramId, entityIdSelector, appCountWidgetParameters.targetDate());
    }


    public Set<TargetCostWidgetDatum> findTargetAppCostWidgetData(Long diagramId,
                                                                  IdSelectionOptions appSelectionOptions,
                                                                  Set<AssessmentBasedSelectionFilter> filterParams,
                                                                  TargetAppCostWidgetParameters targetAppCostWidgetParameters) {

        AggregateOverlayDiagram diagram = aggregateOverlayDiagramDao.getById(diagramId);
        ;
        GenericSelector genericSelector = genericSelectorFactory.applyForKind(diagram.aggregatedEntityKind(), appSelectionOptions);

        Select<Record1<Long>> entityIdSelector = applyFiltersToSelector(genericSelector, filterParams);
        return targetAppCostWidgetDao.findWidgetData(diagramId, entityIdSelector, targetAppCostWidgetParameters.targetDate());
    }


    public Set<CostWidgetDatum> findAppCostWidgetData(Long diagramId,
                                                      Set<AssessmentBasedSelectionFilter> filterParams,
                                                      IdSelectionOptions appSelectionOptions,
                                                      AppCostWidgetParameters appCostWidgetParameters) {

        AggregateOverlayDiagram diagram = aggregateOverlayDiagramDao.getById(diagramId);
        ;

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(diagram.aggregatedEntityKind(), appSelectionOptions);
        Select<Record1<Long>> entityIdSelector = applyFiltersToSelector(genericSelector, filterParams);

        return appCostWidgetDao.findWidgetData(
                diagramId,
                appCostWidgetParameters.costKindIds(),
                appCostWidgetParameters.allocationSchemeId(),
                entityIdSelector,
                Optional.empty());
    }


    public Set<AssessmentRatingsWidgetDatum> findAppAssessmentWidgetData(Long diagramId,
                                                                         Set<AssessmentBasedSelectionFilter> filterParams,
                                                                         IdSelectionOptions appSelectionOptions,
                                                                         AssessmentWidgetParameters assessmentWidgetParameters) {

        AggregateOverlayDiagram diagram = aggregateOverlayDiagramDao.getById(diagramId);

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(diagram.aggregatedEntityKind(), appSelectionOptions);
        Select<Record1<Long>> entityIdSelector = applyFiltersToSelector(genericSelector, filterParams);

        return appAssessmentWidgetDao.findWidgetData(
                diagramId,
                diagram.aggregatedEntityKind(),
                assessmentWidgetParameters.assessmentDefinitionId(),
                entityIdSelector,
                assessmentWidgetParameters.targetDate());
    }


    public Set<AggregatedEntitiesWidgetDatum> findAggregatedEntitiesWidgetData(Long diagramId,
                                                                               Set<AssessmentBasedSelectionFilter> filterParams,
                                                                               IdSelectionOptions idSelectionOptions) {

        AggregateOverlayDiagram diagram = aggregateOverlayDiagramDao.getById(diagramId);
        ;
        GenericSelector genericSelector = genericSelectorFactory.applyForKind(diagram.aggregatedEntityKind(), idSelectionOptions);
        Select<Record1<Long>> entityIdSelector = applyFiltersToSelector(genericSelector, filterParams);

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
