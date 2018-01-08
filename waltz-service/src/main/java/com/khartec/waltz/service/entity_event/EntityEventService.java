package com.khartec.waltz.service.entity_event;


import com.khartec.waltz.data.entity_event.EntityEventDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_event.EntityEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EntityEventService {

    private final EntityEventDao entityEventDao;


    @Autowired
    public EntityEventService(EntityEventDao entityEventDao) {
        this.entityEventDao = entityEventDao;
    }


    public List<EntityEvent> findByEntityReference(EntityReference ref) {
        return entityEventDao.findByEntityReference(ref);
    }
}
