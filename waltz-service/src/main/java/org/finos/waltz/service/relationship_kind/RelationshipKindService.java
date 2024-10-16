package org.finos.waltz.service.relationship_kind;

import org.finos.waltz.data.rel.RelationshipKindDao;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.rel.RelationshipKind;
import org.finos.waltz.model.rel.UpdateRelationshipKindCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;

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

    public RelationshipKind getById(long id) {
        return relationshipKindDao.getById(id);
    }


    public boolean create(RelationshipKind relationshipKind) {
        return relationshipKindDao.create(relationshipKind);
    }


    public boolean remove(Long id) {
        return relationshipKindDao.remove(id);
    }


    public boolean update(long relKindId, UpdateRelationshipKindCommand updateCommand) {
        return relationshipKindDao.update(relKindId, updateCommand);
    }
}
