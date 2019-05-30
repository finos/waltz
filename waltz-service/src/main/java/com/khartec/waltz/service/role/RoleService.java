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
