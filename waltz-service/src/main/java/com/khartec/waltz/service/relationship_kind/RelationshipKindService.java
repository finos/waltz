package com.khartec.waltz.service.relationship_kind;

import com.khartec.waltz.data.rel.RelationshipKindDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.rel.RelationshipKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class RelationshipKindService {

    private final RelationshipKindDao relationshipKindDao;

    @Autowired
    public RelationshipKindService (RelationshipKindDao relationshipKindDao) {
        checkNotNull(relationshipKindDao, "relationshipKindDao cannot be null");
        this.relationshipKindDao = relationshipKindDao;
    }

    public Set<RelationshipKind> findRelationshipKindsBetweenEntites(EntityReference parent, EntityReference target){
        return relationshipKindDao.findRelationshipKindsBetweenEntites(parent, target);
    }


    public Collection<RelationshipKind> findAll() {
        return relationshipKindDao.findAll();
    }
}
