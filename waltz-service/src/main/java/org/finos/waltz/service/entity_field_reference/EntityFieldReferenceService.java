package org.finos.waltz.service.entity_field_reference;

import org.finos.waltz.data.entity_field_reference.EntityFieldReferenceDao;
import org.finos.waltz.model.entity_field_reference.EntityFieldReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class EntityFieldReferenceService {

    private final EntityFieldReferenceDao entityFieldReferenceDao;

    @Autowired
    public EntityFieldReferenceService(EntityFieldReferenceDao entityFieldReferenceDao) {
        checkNotNull(entityFieldReferenceDao, "entityFieldReferenceDao cannot be null");
        this.entityFieldReferenceDao = entityFieldReferenceDao;
    }


    public Set<EntityFieldReference> findAll() {
        return entityFieldReferenceDao.findAll();
    }
}
