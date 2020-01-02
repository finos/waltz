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

package com.khartec.waltz.service.entity_alias;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.entity_alias.EntityAliasDao;
import com.khartec.waltz.model.EntityReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class EntityAliasService {

    private final EntityAliasDao entityAliasDao;

    @Autowired
    public EntityAliasService(EntityAliasDao entityAliasDao) {
        Checks.checkNotNull(entityAliasDao, "entityAliasDao cannot be null");
        this.entityAliasDao = entityAliasDao;
    }


    public List<String> findAliasesForEntityReference(EntityReference ref) {
        return entityAliasDao.findAliasesForEntityReference(ref);
    }


    public int[] updateAliases(EntityReference ref, Collection<String> aliases) {
        return entityAliasDao.updateAliases(ref, aliases);
    }
}
