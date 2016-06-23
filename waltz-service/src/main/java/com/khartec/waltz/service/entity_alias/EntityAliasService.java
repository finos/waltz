package com.khartec.waltz.service.entity_alias;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.entity_alias.EntityAliasDao;
import com.khartec.waltz.model.EntityReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class EntityAliasService {

    private final EntityAliasDao entityAliasDao;

    @Autowired
    public EntityAliasService(EntityAliasDao entityAliasDao) {
        Checks.checkNotNull(entityAliasDao, "entityAliasDao cannot be null");
        this.entityAliasDao = entityAliasDao;
    }


    public List<String> findAliasesForEntityReference(EntityReference ref) {
        return entityAliasDao.findAliasesForEntityReference(ref);
    }


    public int[] updateAliases(EntityReference ref, Collection<String> aliases) {
        return entityAliasDao.updateAliases(ref, aliases);
    }
}
