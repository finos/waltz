package com.khartec.waltz.service.process_diagram;

import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.data.GenericSelectorFactory;
import com.khartec.waltz.data.process_diagram.ProcessDiagramDao;
import com.khartec.waltz.data.process_diagram.ProcessDiagramIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.process_diagram.ImmutableProcessDiagramAndEntities;
import com.khartec.waltz.model.process_diagram.ProcessDiagram;
import com.khartec.waltz.model.process_diagram.ProcessDiagramAndEntities;
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
