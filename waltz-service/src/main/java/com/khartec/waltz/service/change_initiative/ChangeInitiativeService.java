package com.khartec.waltz.service.change_initiative;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.change_initiative.ChangeInitiativeDao;
import com.khartec.waltz.data.change_initiative.search.ChangeInitiativeSearchDao;
import com.khartec.waltz.data.entity_relationship.EntityRelationshipDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.change_initiative.ChangeInitiative;
import com.khartec.waltz.model.entiy_relationship.EntityRelationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class ChangeInitiativeService {

    private final ChangeInitiativeDao baseDao;
    private final ChangeInitiativeSearchDao searchDao;
    private final EntityRelationshipDao relationshipDao;

    @Autowired
    public ChangeInitiativeService(
            ChangeInitiativeDao baseDao,
            ChangeInitiativeSearchDao searchDao,
            EntityRelationshipDao relationshipDao) {

        Checks.checkNotNull(baseDao, "baseDao cannot be null");
        Checks.checkNotNull(searchDao, "searchDao cannot be null");
        Checks.checkNotNull(relationshipDao, "relationshipDao cannot be null");

        this.baseDao = baseDao;
        this.searchDao = searchDao;
        this.relationshipDao = relationshipDao;
    }


    public ChangeInitiative getById(Long id) {
        return baseDao.getById(id);
    }


    public Collection<ChangeInitiative> findForEntityReference(EntityReference ref) {
        return baseDao.findForEntityReference(ref);
    }


    public Collection<ChangeInitiative> search(String query) {
        return searchDao.search(query);
    }


    public Collection<EntityRelationship> getRelatedEntitiesForId(long id) {
        EntityReference ref = ImmutableEntityReference.builder()
                .id(id)
                .kind(EntityKind.CHANGE_INITIATIVE)
                .build();
        return relationshipDao.findRelationshipsInvolving(ref);
    }
}
