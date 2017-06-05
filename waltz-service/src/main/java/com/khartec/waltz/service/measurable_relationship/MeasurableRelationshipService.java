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

package com.khartec.waltz.service.measurable_relationship;

import com.khartec.waltz.data.entity_relationship.EntityRelationshipDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.entity_relationship.EntityRelationship;
import com.khartec.waltz.model.entity_relationship.EntityRelationshipKey;
import com.khartec.waltz.model.entity_relationship.UpdateEntityRelationshipParams;
import com.khartec.waltz.model.measurable_relationship.MeasurableRelationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityReference.mkRef;

@Service
public class MeasurableRelationshipService {

    private final EntityRelationshipDao entityRelationshipDao;


    @Autowired
    public MeasurableRelationshipService(EntityRelationshipDao entityRelationshipDao) {
        checkNotNull(entityRelationshipDao, "entityRelationshipDao cannot be null");
        this.entityRelationshipDao = entityRelationshipDao;
    }


    public Collection<EntityRelationship> findForMeasurable(long measurableId) {
        return entityRelationshipDao
                .findRelationshipsInvolving(mkRef(
                        EntityKind.MEASURABLE,
                        measurableId));
    }


    public boolean remove(EntityRelationshipKey command) {
        return entityRelationshipDao.remove(command);
    }


    public boolean save(MeasurableRelationship measurableRelationship) {
        return false;
    }


    @Deprecated
    public int save(EntityRelationship relationship) {
        return entityRelationshipDao.save(relationship);
    }


    public boolean create(EntityRelationship relationship) {
        return entityRelationshipDao.create(relationship);
    }


    public boolean update(EntityRelationshipKey key, UpdateEntityRelationshipParams params, String username) {
        return entityRelationshipDao.update(key, params, username);
    }
}
