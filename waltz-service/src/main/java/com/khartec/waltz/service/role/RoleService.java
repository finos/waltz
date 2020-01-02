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

package com.khartec.waltz.service.role;

import com.khartec.waltz.data.role.RoleDao;
import com.khartec.waltz.model.role.Role;
import com.khartec.waltz.schema.tables.records.RoleRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotEmpty;

@Service
public class RoleService {

    private static final Logger LOG = LoggerFactory.getLogger(RoleService.class);

    private final RoleDao roleDao;

    @Autowired
    public RoleService(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    public boolean create(String key, String roleName, String description) {
        checkNotEmpty(roleName, "role name cannot be empty");
        checkNotEmpty(key, "key cannot be empty");
        LOG.info("creating new role: {}", roleName);

        RoleRecord role = new RoleRecord();
        role.setKey(key);
        role.setName(roleName);
        role.setDescription(description);
        role.setIsCustom(true);

        return roleDao.create(role);
    }

    public Set<Role> findAllRoles() {
       return roleDao.findAllRoles();
    }
}
