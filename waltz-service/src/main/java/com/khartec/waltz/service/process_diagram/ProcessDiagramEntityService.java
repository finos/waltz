package com.khartec.waltz.service.process_diagram;

import org.finos.waltz.data.process_diagram_entity.ProcessDiagramEntityDao;
import org.finos.waltz.model.process_diagram.ProcessDiagramEntityApplicationAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ProcessDiagramEntityService {

    private final ProcessDiagramEntityDao dao;


    @Autowired
    public ProcessDiagramEntityService(ProcessDiagramEntityDao dao) {
        this.dao = dao;
    }


    public Set<ProcessDiagramEntityApplicationAlignment> findApplicationAlignmentsByDiagramId(Long diagramId){
        return dao.findApplicationAlignmentsByDiagramId(diagramId);
    }
}
