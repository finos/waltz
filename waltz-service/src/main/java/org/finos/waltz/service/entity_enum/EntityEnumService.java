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

package org.finos.waltz.service.entity_enum;

import org.finos.waltz.data.entity_enum.EntityEnumDefinitionDao;
import org.finos.waltz.data.entity_enum.EntityEnumValueDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.entity_enum.EntityEnumDefinition;
import org.finos.waltz.model.entity_enum.EntityEnumValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class EntityEnumService {

    private final EntityEnumDefinitionDao entityEnumDefinitionDao;
    private final EntityEnumValueDao entityEnumValueDao;


    @Autowired
    public EntityEnumService(EntityEnumDefinitionDao entityEnumDefinitionDao,
                             EntityEnumValueDao entityEnumValueDao) {
        checkNotNull(entityEnumDefinitionDao, "entityEnumDefinitionDao cannot be null");
        checkNotNull(entityEnumValueDao, "entityEnumValueDao cannot be null");

        this.entityEnumDefinitionDao = entityEnumDefinitionDao;
        this.entityEnumValueDao = entityEnumValueDao;
    }


    public List<EntityEnumDefinition> findDefinitionsByEntityKind(EntityKind kind) {
        checkNotNull(kind, "kind cannot be null");
        return entityEnumDefinitionDao.findByEntityKind(kind);
    }


    public List<EntityEnumValue> findValuesByEntity(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return entityEnumValueDao.findByEntity(ref);
    }
}
