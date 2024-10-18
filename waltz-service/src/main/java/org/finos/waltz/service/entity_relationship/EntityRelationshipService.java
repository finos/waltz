/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.service.entity_relationship;

import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.entity_relationship.EntityRelationshipDao;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.entity_relationship.Directionality;
import org.finos.waltz.model.entity_relationship.EntityRelationship;
import org.finos.waltz.model.entity_relationship.EntityRelationshipKey;
import org.finos.waltz.model.entity_relationship.RelationshipKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class EntityRelationshipService {

    private final EntityRelationshipDao entityRelationshipDao;
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();


    @Autowired
    public EntityRelationshipService(EntityRelationshipDao entityRelationshipDao) {
        checkNotNull(entityRelationshipDao, "entityRelationshipDao cannot be null");
        this.entityRelationshipDao = entityRelationshipDao;
    }


    public EntityRelationship getById(Long id){
        return entityRelationshipDao.getById(id);
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
            : r -> relationshipKinds.contains(RelationshipKind.valueOf(r.relationship()));
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

    public void migrateEntityRelationships(EntityReference sourceReference, EntityReference targetReference, String userId) {
        entityRelationshipDao.migrateEntityRelationships(sourceReference, targetReference, userId);
    }

    public Collection<EntityRelationship> getEntityRelationshipsByKind(org.finos.waltz.model.rel.RelationshipKind relationshipKind) {
        return entityRelationshipDao.getEntityRelationshipsByKind(relationshipKind);
    }
}
