package org.finos.waltz.service.aggregate_overlay_diagram;

import org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramCalloutDao;
import org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramInstanceDao;
import org.finos.waltz.model.aggregate_overlay_diagram.AggregateOverlayDiagramCallout;
import org.finos.waltz.model.aggregate_overlay_diagram.AggregateOverlayDiagramInstance;
import org.finos.waltz.model.aggregate_overlay_diagram.OverlayDiagramInstanceCreateCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AggregateOverlayDiagramInstanceService {


    private final AggregateOverlayDiagramInstanceDao aggregateOverlayDiagramInstanceDao;

    @Autowired
    public AggregateOverlayDiagramInstanceService(AggregateOverlayDiagramInstanceDao aggregateOverlayDiagramInstanceDao) {
        this.aggregateOverlayDiagramInstanceDao = aggregateOverlayDiagramInstanceDao;
    }


    public Set<AggregateOverlayDiagramInstance> findAll() {
        return aggregateOverlayDiagramInstanceDao.findAll();
    }

    public Set<AggregateOverlayDiagramInstance> findByDiagramId(Long diagramId) {
        return aggregateOverlayDiagramInstanceDao.findByDiagramId(diagramId);
    }

    public AggregateOverlayDiagramInstance getById(Long instanceId) {
        return aggregateOverlayDiagramInstanceDao.getById(instanceId);
    }

    public int createInstance(OverlayDiagramInstanceCreateCommand createCommand,
                              String username) {
        return aggregateOverlayDiagramInstanceDao.createInstance(createCommand, username);
    }

}
