package org.finos.waltz.service.aggregate_overlay_diagram;

import org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramCalloutDao;
import org.finos.waltz.model.aggregate_overlay_diagram.AggregateOverlayDiagramCallout;
import org.finos.waltz.model.aggregate_overlay_diagram.DiagramCalloutCreateCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AggregateOverlayDiagramCalloutService {


    private final AggregateOverlayDiagramCalloutDao aggregateOverlayDiagramCalloutDao;

    @Autowired
    public AggregateOverlayDiagramCalloutService(AggregateOverlayDiagramCalloutDao aggregateOverlayDiagramCalloutDao) {
        this.aggregateOverlayDiagramCalloutDao = aggregateOverlayDiagramCalloutDao;
    }


    public Set<AggregateOverlayDiagramCallout> findByDiagramInstanceId(Long diagramInstanceId) {
        return aggregateOverlayDiagramCalloutDao.findByDiagramInstanceId(diagramInstanceId);
    }


    public Integer create(DiagramCalloutCreateCommand createCommand) {
        return aggregateOverlayDiagramCalloutDao.create(createCommand);
    }

    public Integer update(AggregateOverlayDiagramCallout callout) {
        return aggregateOverlayDiagramCalloutDao.update(callout);
    }

    public Integer delete(long calloutId) {
        return aggregateOverlayDiagramCalloutDao.delete(calloutId);
    }
}
