/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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

package com.khartec.waltz.service.entity_relationship;

import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.data.GenericSelectorFactory;
import com.khartec.waltz.data.entity_relationship.EntityRelationshipDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.entity_relationship.Directionality;
import com.khartec.waltz.model.entity_relationship.EntityRelationship;
import com.khartec.waltz.model.entity_relationship.EntityRelationshipKey;
import com.khartec.waltz.model.entity_relationship.RelationshipKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class EntityRelationshipService {

    private final EntityRelationshipDao entityRelationshipDao;
    private final GenericSelectorFactory genericSelectorFactory;


    @Autowired
    public EntityRelationshipService(EntityRelationshipDao entityRelationshipDao,
                                     GenericSelectorFactory genericSelectorFactory) {
        checkNotNull(entityRelationshipDao, "entityRelationshipDao cannot be null");
        checkNotNull(genericSelectorFactory, "genericSelectorFactory cannot be null");

        this.entityRelationshipDao = entityRelationshipDao;
        this.genericSelectorFactory = genericSelectorFactory;
    }


    public Collection<EntityRelationship> findForEntity(EntityReference ref,
                                                        Directionality directionality,
                                                        List<RelationshipKind> relationshipKinds) {


        Predicate<EntityRelationship> directionalityFilter = mkDirectionalityFilter(ref, directionality);
        Predicate<EntityRelationship> relationshipKindFilter = mkRelationshipKindFilter(relationshipKinds);

        Collection<EntityRelationship> relationships = entityRelationshipDao.findRelationshipsInvolving(ref);

        return relationships
                .stream()
                .filter(directionalityFilter)
                .filter(relationshipKindFilter)
                .collect(Collectors.toList());
    }


    public Boolean removeRelationship(EntityRelationshipKey entityRelationshipKey) {
        return entityRelationshipDao.remove(entityRelationshipKey);
    }


    public Boolean createRelationship(EntityRelationship entityRelationship) {
        return entityRelationshipDao.create(entityRelationship);
    }


    private Predicate<EntityRelationship> mkRelationshipKindFilter(List<RelationshipKind> relationshipKinds) {
        return relationshipKinds.isEmpty()
            ? r -> true
            : r -> relationshipKinds.contains(r.relationship());
    }


    private Predicate<EntityRelationship> mkDirectionalityFilter(EntityReference ref, Directionality directionality) {
        switch (directionality) {
            case ANY:
                return (r) -> true;
            case SOURCE:
                return (r) -> r.a().equals(ref);
            case TARGET:
                return (r) -> r.b().equals(ref);
            default:
                return (r) -> false;
        }
    }


    public Collection<EntityRelationship> findForGenericEntitySelector(IdSelectionOptions selectionOptions) {
        GenericSelector selector = genericSelectorFactory.apply(selectionOptions);
        return entityRelationshipDao.findForGenericEntitySelector(selector);
    }

    public int deleteForGenericEntitySelector(IdSelectionOptions selectionOptions) {
        GenericSelector selector = genericSelectorFactory.apply(selectionOptions);
        return entityRelationshipDao.deleteForGenericEntitySelector(selector);
    }
}
