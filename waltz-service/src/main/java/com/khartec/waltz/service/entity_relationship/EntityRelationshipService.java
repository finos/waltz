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
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();


    @Autowired
    public EntityRelationshipService(EntityRelationshipDao entityRelationshipDao) {
        checkNotNull(entityRelationshipDao, "entityRelationshipDao cannot be null");
        this.entityRelationshipDao = entityRelationshipDao;
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
