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

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.change_initiative.ChangeInitiativeDao;
import com.khartec.waltz.data.change_initiative.search.ChangeInitiativeSearchDao;
import com.khartec.waltz.data.entity_relationship.EntityRelationshipDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.change_initiative.ChangeInitiative;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.entiy_relationship.EntityRelationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

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
        return search(query, EntitySearchOptions.mkForEntity(EntityKind.CHANGE_INITIATIVE));
    }


    public Collection<ChangeInitiative> search(String query, EntitySearchOptions options) {
        return searchDao.search(query, options);
    }


    public Collection<EntityRelationship> getRelatedEntitiesForId(long id) {
        EntityReference ref = ImmutableEntityReference.builder()
                .id(id)
                .kind(EntityKind.CHANGE_INITIATIVE)
                .build();
        return relationshipDao.findRelationshipsInvolving(ref);
    }
}
