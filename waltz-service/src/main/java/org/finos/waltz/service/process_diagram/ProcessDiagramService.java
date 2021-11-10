package org.finos.waltz.service.process_diagram;

import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.process_diagram.ProcessDiagramDao;
import org.finos.waltz.data.process_diagram.ProcessDiagramIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.process_diagram.ImmutableProcessDiagramAndEntities;
import org.finos.waltz.model.process_diagram.ProcessDiagram;
import org.finos.waltz.model.process_diagram.ProcessDiagramAndEntities;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ProcessDiagramService {

    private final ProcessDiagramDao dao;
    private GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();
    private ProcessDiagramIdSelectorFactory processDiagramIdSelectorFactory = new ProcessDiagramIdSelectorFactory();


    @Autowired
    public ProcessDiagramService(ProcessDiagramDao dao) {
        this.dao = dao;
    }


    public ProcessDiagram getByExternalId(String externalId) {
        return dao.getDiagramByExternalId(externalId);
    }


    public Set<ProcessDiagram> findByGenericSelector(EntityKind targetKind,
                                                     IdSelectionOptions selectionOptions) {
        GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, selectionOptions);
        return dao.findByGenericSelector(genericSelector);
    }


    public Set<ProcessDiagram> findBySelector(IdSelectionOptions selectionOptions) {
        Select<Record1<Long>> selector = processDiagramIdSelectorFactory.apply(selectionOptions);
        return dao.findBySelector(selector);
    }


    public ProcessDiagramAndEntities getDiagramAndEntitiesById(long diagramId) {
        return ImmutableProcessDiagramAndEntities
                .builder()
                .diagram(dao.getDiagramById(diagramId))
                .entities(dao.findDiagramEntitiesById(diagramId))
                .build();
    }
}
