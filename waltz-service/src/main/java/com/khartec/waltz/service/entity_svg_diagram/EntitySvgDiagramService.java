package com.khartec.waltz.service.entity_svg_diagram;

import com.khartec.waltz.data.entity_svg_diagram.EntitySvgDiagramDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_svg_diagram.EntitySvgDiagram;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class EntitySvgDiagramService {

    private final EntitySvgDiagramDao entitySvgDiagramDao;


    @Autowired
    public EntitySvgDiagramService(EntitySvgDiagramDao entitySvgDiagramDao) {
        checkNotNull(entitySvgDiagramDao, "entitySvgDiagramDao cannot be null");
        this.entitySvgDiagramDao = entitySvgDiagramDao;
    }


    public List<EntitySvgDiagram> findForEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return entitySvgDiagramDao.findForEntityReference(ref);
    }

}
