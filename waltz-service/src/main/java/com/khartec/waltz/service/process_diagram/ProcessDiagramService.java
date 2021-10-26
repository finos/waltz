package com.khartec.waltz.service.process_diagram;

import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.data.GenericSelectorFactory;
import com.khartec.waltz.data.process_diagram.ProcessDiagramDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.process_diagram.ImmutableProcessDiagramAndEntities;
import com.khartec.waltz.model.process_diagram.ProcessDiagram;
import com.khartec.waltz.model.process_diagram.ProcessDiagramAndEntities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ProcessDiagramService {

    private final ProcessDiagramDao dao;
    private GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();


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


    public Set<ProcessDiagram> findBySelector(EntityKind targetKind,
                                              IdSelectionOptions selectionOptions) {
        GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, selectionOptions);
        return dao.findByGenericSelector(genericSelector);
    }


    public ProcessDiagramAndEntities getDiagramAndEntitiesById(long diagramId) {
        return ImmutableProcessDiagramAndEntities
                .builder()
                .diagram(dao.getDiagramById(diagramId))
                .entities(dao.findDiagramEntitiesById(diagramId))
                .build();
    }
}
