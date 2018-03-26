package com.khartec.waltz.service.entity_enum;

import com.khartec.waltz.data.entity_enum.EntityEnumDefinitionDao;
import com.khartec.waltz.data.entity_enum.EntityEnumValueDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_enum.EntityEnumDefinition;
import com.khartec.waltz.model.entity_enum.EntityEnumValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class EntityEnumService {

    private final EntityEnumDefinitionDao entityEnumDefinitionDao;
    private final EntityEnumValueDao entityEnumValueDao;


    @Autowired
    public EntityEnumService(EntityEnumDefinitionDao entityEnumDefinitionDao,
                             EntityEnumValueDao entityEnumValueDao) {
        checkNotNull(entityEnumDefinitionDao, "entityEnumDefinitionDao cannot be null");
        checkNotNull(entityEnumValueDao, "entityEnumValueDao cannot be null");

        this.entityEnumDefinitionDao = entityEnumDefinitionDao;
        this.entityEnumValueDao = entityEnumValueDao;
    }


    public List<EntityEnumDefinition> findDefinitionsByEntityKind(EntityKind kind) {
        checkNotNull(kind, "kind cannot be null");
        return entityEnumDefinitionDao.findByEntityKind(kind);
    }


    public List<EntityEnumValue> findValuesByEntity(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return entityEnumValueDao.findByEntity(ref);
    }
}
