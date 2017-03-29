/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.change_initiative;

import com.khartec.waltz.data.change_initiative.ChangeInitiativeDao;
import com.khartec.waltz.data.change_initiative.search.ChangeInitiativeSearchDao;
import com.khartec.waltz.data.entity_relationship.EntityRelationshipDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.change_initiative.ChangeInitiative;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.entiy_relationship.EntityRelationship;
import com.khartec.waltz.model.entiy_relationship.EntityRelationshipChangeCommand;
import com.khartec.waltz.model.entiy_relationship.RelationshipKind;
import com.khartec.waltz.service.entity_relationship.EntityRelationshipUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityKind.CHANGE_INITIATIVE;
import static com.khartec.waltz.model.EntityReference.mkRef;

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

        checkNotNull(baseDao, "baseDao cannot be null");
        checkNotNull(searchDao, "searchDao cannot be null");
        checkNotNull(relationshipDao, "relationshipDao cannot be null");

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
        return search(query, EntitySearchOptions.mkForEntity(CHANGE_INITIATIVE));
    }


    public Collection<ChangeInitiative> search(String query, EntitySearchOptions options) {
        return searchDao.search(query, options);
    }


    public Collection<EntityRelationship> getRelatedEntitiesForId(long id) {
        EntityReference ref = ImmutableEntityReference.builder()
                .id(id)
                .kind(CHANGE_INITIATIVE)
                .build();
        return relationshipDao.findRelationshipsInvolving(ref);
    }


    public boolean addEntityRelationship(long changeInitiativeId,
                                         EntityRelationshipChangeCommand command) {
        checkNotNull(command, "command cannot be null");

        return relationshipDao.save(mkEntityRelationship(changeInitiativeId, command)) == 1;
    }


    public boolean removeEntityRelationship(long changeInitiativeId,
                                            EntityRelationshipChangeCommand command) {
        checkNotNull(command, "command cannot be null");

        return relationshipDao.remove(mkEntityRelationship(changeInitiativeId, command)) == 1;
    }


    private EntityRelationship mkEntityRelationship(long changeInitiativeId, EntityRelationshipChangeCommand command) {
        EntityReference entityReference = command.entityReference();
        RelationshipKind relationship = command.relationship();

        return EntityRelationshipUtilities.mkEntityRelationship(
                mkRef(CHANGE_INITIATIVE, changeInitiativeId),
                entityReference,
                relationship)
                .orElseThrow(() -> new RuntimeException(String.format(
                        "Could not build a valid relationship for kind: %s between %s and %s",
                        relationship,
                        CHANGE_INITIATIVE,
                        entityReference.kind()
                )));
    }

}
