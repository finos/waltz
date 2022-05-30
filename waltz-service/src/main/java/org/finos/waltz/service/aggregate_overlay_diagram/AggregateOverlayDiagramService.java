package org.finos.waltz.service.aggregate_overlay_diagram;

import org.finos.waltz.data.aggregate_overlay_diagram.*;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.data.assessment_rating.AssessmentRatingBasedGenericSelectorFactory;
import org.finos.waltz.model.AssessmentBasedSelectionFilter;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.aggregate_overlay_diagram.AggregateOverlayDiagram;
import org.finos.waltz.model.aggregate_overlay_diagram.AggregateOverlayDiagramInfo;
import org.finos.waltz.model.aggregate_overlay_diagram.BackingEntity;
import org.finos.waltz.model.aggregate_overlay_diagram.ImmutableAggregateOverlayDiagramInfo;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.*;
import org.jooq.Record1;
import org.jooq.ResultQuery;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.finos.waltz.data.assessment_rating.AssessmentRatingBasedGenericSelectorFactory.prepareFilteredSelection;

@Service
public class AggregateOverlayDiagramService {

    public static final ApplicationIdSelectorFactory APPLICATION_ID_SELECTOR_FACTORY = new ApplicationIdSelectorFactory();

    private final AggregateOverlayDiagramDao aggregateOverlayDiagramDao;
    private final AppCountWidgetDao appCountWidgetDao;
    private final TargetAppCostWidgetDao targetAppCostWidgetDao;
    private final AppCostWidgetDao appCostWidgetDao;
    private final AppAssessmentWidgetDao appAssessmentWidgetDao;
    private final BackingEntityWidgetDao backingEntityWidgetDao;

    @Autowired
    public AggregateOverlayDiagramService(AggregateOverlayDiagramDao aggregateOverlayDiagramDao,
                                          AppCountWidgetDao appCountWidgetDao,
                                          TargetAppCostWidgetDao targetAppCostWidgetDao,
                                          AppAssessmentWidgetDao appAssessmentWidgetDao,
                                          BackingEntityWidgetDao backingEntityWidgetDao,
                                          AppCostWidgetDao appCostWidgetDao) {
        this.aggregateOverlayDiagramDao = aggregateOverlayDiagramDao;
        this.appCountWidgetDao = appCountWidgetDao;
        this.targetAppCostWidgetDao = targetAppCostWidgetDao;
        this.appCostWidgetDao = appCostWidgetDao;
        this.appAssessmentWidgetDao = appAssessmentWidgetDao;
        this.backingEntityWidgetDao = backingEntityWidgetDao;
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
                                                        LocalDate targetStateDate) {

        Select<Record1<Long>> entityIdSelector = prepareFilteredSelection(EntityKind.APPLICATION, appSelectionOptions, filterParams);
        return appCountWidgetDao.findWidgetData(diagramId, entityIdSelector, targetStateDate);
    }


    public Set<TargetCostWidgetDatum> findTargetAppCostWidgetData(Long diagramId,
                                                                  IdSelectionOptions appSelectionOptions,
                                                                  Optional<AssessmentBasedSelectionFilter> filterParams,
                                                                  LocalDate targetStateDate) {

        Select<Record1<Long>> entityIdSelector = prepareFilteredSelection(EntityKind.APPLICATION, appSelectionOptions, filterParams);
        return targetAppCostWidgetDao.findWidgetData(diagramId, entityIdSelector, targetStateDate);
    }


    public Set<CostWidgetDatum> findAppCostWidgetData(Long diagramId,
                                                      Optional<AssessmentBasedSelectionFilter> filterParams,
                                                      IdSelectionOptions appSelectionOptions,
                                                      Set<Long> costKinds,
                                                      long allocationSchemeId) {

        Select<Record1<Long>> entityIdSelector = prepareFilteredSelection(EntityKind.APPLICATION, appSelectionOptions, filterParams);
        return appCostWidgetDao.findWidgetData(
                diagramId,
                costKinds,
                allocationSchemeId,
                entityIdSelector);
    }


    public Set<AssessmentRatingsWidgetDatum> findAppAssessmentWidgetData(Long diagramId,
                                                                         Optional<AssessmentBasedSelectionFilter> filterParams,
                                                                         long assessmentId,
                                                                         IdSelectionOptions appSelectionOptions) {

        Select<Record1<Long>> entityIdSelector = prepareFilteredSelection(EntityKind.APPLICATION, appSelectionOptions, filterParams);
        return appAssessmentWidgetDao.findWidgetData(
                diagramId,
                assessmentId,
                entityIdSelector);
    }


    public Set<BackingEntityWidgetDatum> findBackingEntityWidgetData(Long diagramId) {
        return backingEntityWidgetDao.findWidgetData(diagramId);
    }


}
